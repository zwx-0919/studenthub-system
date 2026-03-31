package com.it.zwx.studenthub_system.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 考试提醒 WebSocket（简化可用版）
 *
 * 路径：/ws/chat/exam-remind
 *
 * 使用方式建议：
 * - 前端连接后发送一条消息： "register:{userId}"
 * - 服务端记录 session 和 userId 的映射
 * - 通过 {@link #sendToUser(String, String)} 向指定用户推送消息
 */
@ServerEndpoint(value = "/ws/chat/exam-remind")
public class ExamReminderWebSocket {

    // userId -> session
    private static final Map<String, Session> USER_SESSIONS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        // 默认不做强绑定，等待前端通过 register 消息上报 userId
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        if (message == null) {
            return;
        }

        String msg = message.trim();
        // register:{userId}
        if (msg.startsWith("register:")) {
            String userId = msg.substring("register:".length()).trim();
            if (!userId.isEmpty()) {
                USER_SESSIONS.put(userId, session);
                session.getUserProperties().put("userId", userId);
                session.getBasicRemote().sendText("registered:" + userId);
            }
            return;
        }

        // echo（便于前端联调）
        session.getBasicRemote().sendText("received:" + msg);
    }

    @OnClose
    public void onClose(Session session) {
        Object userId = session.getUserProperties().get("userId");
        if (userId != null) {
            USER_SESSIONS.remove(String.valueOf(userId));
        } else {
            // fallback：遍历删除
            USER_SESSIONS.entrySet().removeIf(e -> e.getValue().equals(session));
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // 不抛出异常，避免干扰容器
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (IOException ignored) {
        }
    }

    /**
     * 推送消息给指定用户
     * @param userId 用户ID
     * @param message 消息内容
     */
    public static void sendToUser(String userId, String message) {
        if (userId == null || message == null) {
            return;
        }
        Session session = USER_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException ignored) {
                // 发送失败就让客户端重连
            }
        }
    }
}

