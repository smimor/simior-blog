package org.simior.config;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.simior.handler.MyWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    @Value("${blog.cors.allowed-origins:}")
    private String allowedOrigins;

    private final MyWebSocketHandler myWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 根据配置决定允许的 WebSocket 来源
        String[] origins;
        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            // 未配置时仅允许本地来源
            origins = new String[]{"http://localhost:*", "http://127.0.0.1:*"};
        } else {
            origins = allowedOrigins.split(",");
            for (int i = 0; i < origins.length; i++) {
                origins[i] = origins[i].trim();
            }
        }

        registry.addHandler(myWebSocketHandler, "/ws/{sid}")
                .addInterceptors(new AuthHandshakeInterceptor())
                .setAllowedOriginPatterns(origins);
    }

    /**
     * WebSocket 握手拦截器，用于身份认证
     */
    private static class AuthHandshakeInterceptor implements HandshakeInterceptor {

        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Map<String, Object> attributes) {
            if (request instanceof ServletServerHttpRequest servletRequest) {
                HttpServletRequest httpRequest = servletRequest.getServletRequest();

                // 从 URL 参数中获取 token
                String token = httpRequest.getParameter("token");

                if (StrUtil.isNotBlank(token)) {
                    // 移除 Bearer 前缀
                    if (token.startsWith("Bearer ")) {
                        token = token.substring(7);
                    }

                    // 通过 Sa-Token 验证 token
                    Object loginId = StpUtil.getLoginIdByToken(token);
                    if (loginId != null) {
                        attributes.put("userId", loginId.toString());
                        return true;
                    }
                }
            }
            // 认证失败，拒绝连接
            return false;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Exception exception) {
            // do nothing
        }
    }
}
