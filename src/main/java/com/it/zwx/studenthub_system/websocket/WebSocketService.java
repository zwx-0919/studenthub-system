package com.it.zwx.studenthub_system.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class WebSocketService {

    private final Map<Integer, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void bind(Integer userId, WebSocketSession session) {
        sessions.put(userId, session);
    }

    public void unbind(WebSocketSession session) {
        sessions.entrySet().removeIf(e -> e.getValue().getId().equals(session.getId()));
    }

    public void pushToUser(Integer userId, String message) {
        WebSocketSession session = sessions.get(userId);
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            log.error("WebSocket push failed userId={}", userId, e);
        }
    }

    /**
     * 统一提醒弹窗消息格式，前端可按 type 做分流处理。
     *
     */
    public void pushReminder(Integer userId, Integer type, String text, Integer bizId) {
        String payload = String.format(
                "{\"type\":%d,\"text\":\"%s\",\"bizId\":%s}",
                type == null ? 1 : type,
                escapeJson(text),
                bizId == null ? "null" : bizId.toString()
        );
        pushToUser(userId, payload);
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

