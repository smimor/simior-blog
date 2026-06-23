package org.simior.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class MyWebSocketHandler extends TextWebSocketHandler {

    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * 客户端连接建立时调用，可发送欢迎消息。
     *
     * @param session 当前会话
     * @throws Exception 抛出异常
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = extractUserId(session);
        if (userId == null) {
            session.close();
            return;
        }
        sessions.put(userId, session);
        // 向客户端发送连接成功消息
        session.sendMessage(new TextMessage("连接成功！当前在线人数：" + sessions.size()));
    }

    /**
     * 接收客户端发送的消息，并广播给其他用户。
     *
     * @param session 当前会话
     * @param message 接收到的消息
     * @throws Exception 抛出异常
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String senderId = extractUserId(session);
        log.info("接收到客户端消息: {}，来自用户: {}", message.getPayload(), senderId);

        // 广播消息给其他在线用户（排除发送者自身）
        broadcast(senderId, message.getPayload());
    }

    /**
     * 连接关闭时调用。
     *
     * @param session 当前会话
     * @param status  关闭状态
     * @throws Exception 抛出异常
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = extractUserId(session);
        sessions.remove(userId);
        log.info("用户 {} 从WebSocket断开连接，原因: {}", userId, status);
    }

    /**
     * 从session属性中提取用户ID（由握手拦截器设置）。
     *
     * @param session 当前会话
     * @return 用户ID，如果无法提取则返回null
     */
    private String extractUserId(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        return userId != null ? userId.toString() : null;
    }

    /**
     * 发送消息给指定用户
     */
    public static void sendToUser(String userId, String message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                log.info("发送WebSocket消息给用户 {}: {}", userId, message);
            } catch (IOException e) {
                log.error("发送WebSocket消息失败，移除用户会话: {}", userId, e);
                sessions.remove(userId);
            }
        } else {
            log.warn("用户 {} 未连接到WebSocket或会话已关闭", userId);
        }
    }

    /**
     * 广播消息给所有在线用户（排除发送者）
     */
    public static void broadcast(String senderId, String message) {
        String formattedMessage = "用户 " + senderId + " 说：" + message;

        // 先清理已关闭的会话
        sessions.entrySet().removeIf(entry -> !entry.getValue().isOpen());

        // 广播消息（排除发送者）
        for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            String userId = entry.getKey();
            WebSocketSession session = entry.getValue();

            // 跳过发送者自身
            if (userId.equals(senderId)) {
                continue;
            }

            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(formattedMessage));
                }
            } catch (IOException e) {
                log.error("广播WebSocket消息失败，移除用户会话: {}", userId, e);
                sessions.remove(userId);
            }
        }
    }
}
