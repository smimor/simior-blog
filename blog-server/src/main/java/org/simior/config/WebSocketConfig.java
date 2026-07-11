package org.simior.config;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.simior.handler.MyWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

/**
 * WebSocket 配置
 * <p>
 * Token 传递方式：通过 Sec-WebSocket-Protocol 子协议头传递，而非 URL 查询参数。
 * URL 参数会被写入 Nginx access log、浏览器历史、代理日志，不适合传递凭据。
 *
 * <p>客户端连接示例：
 * <pre>
 *   new WebSocket("ws://host/ws/xxx", ["Bearer." + token])
 * </pre>
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private static final String TOKEN_PROTOCOL_PREFIX = "Bearer.";
    private final MyWebSocketHandler myWebSocketHandler;

    @Value("${blog.cors.allowed-origins:}")
    private String allowedOrigins;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String[] origins;
        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            origins = new String[]{"http://localhost:*", "http://127.0.0.1:*"};
        } else {
            origins = allowedOrigins.split(",");
            for (int i = 0; i < origins.length; i++) {
                origins[i] = origins[i].trim();
            }
        }

        registry.addHandler(myWebSocketHandler, "/ws/{sid}")
                .addInterceptors(new AuthHandshakeInterceptor())
                .setHandshakeHandler(new BearerHandshakeHandler())
                .setAllowedOriginPatterns(origins);
    }

    /**
     * 握手拦截器：从 Sec-WebSocket-Protocol 头提取 Token 并验证
     */
    private static class AuthHandshakeInterceptor implements HandshakeInterceptor {

        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Map<String, Object> attributes) {
            String token = extractTokenFromProtocol(request);
            if (StrUtil.isBlank(token)) {
                return false;
            }

            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId == null) {
                return false;
            }

            attributes.put("userId", loginId.toString());
            // 在响应头回写标准子协议名，告知客户端协议协商成功
            response.getHeaders().set(WebSocketHttpHeaders.SEC_WEBSOCKET_PROTOCOL, "bearer");
            return true;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Exception exception) {
        }

        /**
         * 从 Sec-WebSocket-Protocol 头中提取 Bearer Token
         * 客户端发送格式：Sec-WebSocket-Protocol: Bearer.<token>, bearer
         */
        private String extractTokenFromProtocol(ServerHttpRequest request) {
            String protocols = request.getHeaders().getFirst(WebSocketHttpHeaders.SEC_WEBSOCKET_PROTOCOL);
            if (protocols == null) return null;

            for (String protocol : protocols.split(",")) {
                String trimmed = protocol.trim();
                if (trimmed.startsWith(TOKEN_PROTOCOL_PREFIX)) {
                    return trimmed.substring(TOKEN_PROTOCOL_PREFIX.length());
                }
            }
            return null;
        }
    }

    /**
     * 自定义握手处理器：为未认证的连接分配匿名 Principal，避免握手失败
     */
    private static class BearerHandshakeHandler extends DefaultHandshakeHandler {
        @Override
        protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                          Map<String, Object> attributes) {
            String userId = (String) attributes.get("userId");
            if (userId != null) {
                return () -> "user-" + userId;
            }
            return () -> "anonymous-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }
}
