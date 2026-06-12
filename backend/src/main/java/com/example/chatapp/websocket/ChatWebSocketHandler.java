package com.example.chatapp.websocket;

import com.example.chatapp.model.req.ChatStreamRequest;
import com.example.chatapp.model.req.ChatWsIncomingMessage;
import com.example.chatapp.service.ChatService;
import com.example.chatapp.stream.WebSocketStreamSink;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final Executor chatExecutor;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final ConcurrentHashMap<String, AtomicBoolean> streamingFlags = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, WebSocketStreamSink> activeSinks = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(ChatService chatService,
                                @Qualifier("chatExecutor") Executor chatExecutor,
                                ObjectMapper objectMapper,
                                Validator validator) {
        this.chatService = chatService;
        this.chatExecutor = chatExecutor;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ChatWsIncomingMessage incoming = objectMapper.readValue(message.getPayload(), ChatWsIncomingMessage.class);
        if (incoming.getAction() == null) {
            sendError(session, "action is required");
            return;
        }

        switch (incoming.getAction()) {
            case "chat" -> handleChat(session, incoming);
            case "cancel" -> handleCancel(session);
            default -> sendError(session, "Unsupported action: " + incoming.getAction());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        cleanupSession(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        cleanupSession(session.getId());
    }

    private void handleChat(WebSocketSession session, ChatWsIncomingMessage incoming) throws IOException {
        String sessionId = session.getId();
        AtomicBoolean streaming = streamingFlags.computeIfAbsent(sessionId, ignored -> new AtomicBoolean(false));
        if (!streaming.compareAndSet(false, true)) {
            sendError(session, "Another chat is in progress on this connection");
            return;
        }

        ChatStreamRequest request = toChatRequest(incoming);
        Set<ConstraintViolation<ChatStreamRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            streaming.set(false);
            sendError(session, violations.iterator().next().getMessage());
            return;
        }

        chatExecutor.execute(() -> {
            WebSocketStreamSink sink = new WebSocketStreamSink(session, objectMapper);
            activeSinks.put(sessionId, sink);
            try {
                chatService.streamChat(request, sink);
            } finally {
                activeSinks.remove(sessionId);
                streaming.set(false);
            }
        });
    }

    private void handleCancel(WebSocketSession session) {
        WebSocketStreamSink sink = activeSinks.get(session.getId());
        if (sink != null) {
            sink.cancel();
        }
    }

    private ChatStreamRequest toChatRequest(ChatWsIncomingMessage incoming) {
        ChatStreamRequest request = new ChatStreamRequest();
        request.setSessionId(incoming.getSessionId());
        request.setModelId(incoming.getModelId());
        request.setMessage(incoming.getMessage());
        return request;
    }

    private void sendError(WebSocketSession session, String message) throws IOException {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("event", "error");
        envelope.put("data", Map.of("message", message));
        synchronized (session) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(envelope)));
            }
        }
    }

    private void cleanupSession(String sessionId) {
        WebSocketStreamSink sink = activeSinks.remove(sessionId);
        if (sink != null) {
            sink.cancel();
        }
        streamingFlags.remove(sessionId);
    }
}
