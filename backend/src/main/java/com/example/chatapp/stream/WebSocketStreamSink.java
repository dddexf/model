package com.example.chatapp.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class WebSocketStreamSink implements StreamSink {

    private final WebSocketSession session;
    private final ObjectMapper objectMapper;
    private volatile boolean cancelled;

    public WebSocketStreamSink(WebSocketSession session, ObjectMapper objectMapper) {
        this.session = session;
        this.objectMapper = objectMapper;
    }

    public void cancel() {
        cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void emit(String event, Object data) {
        if (cancelled || session == null || !session.isOpen()) {
            return;
        }
        try {
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("event", event);
            envelope.put("data", data);
            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(envelope)));
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("WebSocket connection broken", ex);
        }
    }

    @Override
    public void complete() {
        // Keep the WebSocket connection open for subsequent chat requests.
    }
}
