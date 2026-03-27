package com.example.chatapp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.chatapp.domain.entity.ChatMessage;
import com.example.chatapp.domain.entity.ChatSession;
import com.example.chatapp.domain.enums.MessageRole;
import com.example.chatapp.domain.enums.MessageStatus;
import com.example.chatapp.dto.ChatStreamRequest;
import com.example.chatapp.exception.NotFoundException;
import com.example.chatapp.repository.AiModelRepository;
import com.example.chatapp.repository.ChatMessageRepository;
import com.example.chatapp.repository.ChatSessionRepository;
import com.example.chatapp.repository.projection.AiModelProviderView;
import com.example.chatapp.service.client.LlmMessage;
import com.example.chatapp.service.client.OpenAiCompatibleClient;
import com.example.chatapp.stream.StreamSink;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AiModelRepository aiModelRepository;
    private final ContextBuilder contextBuilder;
    private final AesKeyCipher aesKeyCipher;
    private final OpenAiCompatibleClient openAiCompatibleClient;
    private final int recentRounds;

    public ChatService(ChatSessionRepository chatSessionRepository,
                       ChatMessageRepository chatMessageRepository,
                       AiModelRepository aiModelRepository,
                       ContextBuilder contextBuilder,
                       AesKeyCipher aesKeyCipher,
                       OpenAiCompatibleClient openAiCompatibleClient,
                       @Value("${app.stream.recent-rounds:10}") int recentRounds) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.aiModelRepository = aiModelRepository;
        this.contextBuilder = contextBuilder;
        this.aesKeyCipher = aesKeyCipher;
        this.openAiCompatibleClient = openAiCompatibleClient;
        this.recentRounds = recentRounds;
    }

    public void streamChat(ChatStreamRequest request, StreamSink sink) {
        ChatSession session;
        AiModelProviderView model;
        ChatMessage assistantMessage;
        try {
            session = requireSession(request.getSessionId());
            model = requireModel(request.getModelId());
            assistantMessage = saveUserAndAssistantPlaceholder(session.getId(), model.getId(), request.getMessage());
        } catch (Exception ex) {
            sink.emit("error", Map.of("message", safeMessage(ex)));
            sink.complete();
            return;
        }

        StringBuilder generated = new StringBuilder();
        sink.emit("start", Map.of("assistantMessageId", assistantMessage.getId()));

        try {
            List<ChatMessage> history = chatMessageRepository.findBySessionIdOrderBySeqNoAsc(session.getId());
            List<LlmMessage> context = contextBuilder.build(history, model.getMaxContextTokens(), recentRounds);
            String apiKey = aesKeyCipher.decrypt(model.getProviderApiKeyCipher());

            openAiCompatibleClient.streamChat(
                    model.getProviderBaseUrl(),
                    apiKey,
                    model.getModelCode(),
                    context,
                    delta -> {
                        generated.append(delta);
                        sink.emit("delta", Map.of("delta", delta));
                    }
            );

            finalizeAssistantMessage(assistantMessage.getId(), generated.toString(), MessageStatus.DONE);
            updateSessionAfterChat(session.getId(), model.getId());
            sink.emit("done", Map.of(
                    "assistantMessageId", assistantMessage.getId(),
                    "content", generated.toString()
            ));
        } catch (Exception ex) {
            finalizeAssistantMessage(assistantMessage.getId(), generated.toString(), MessageStatus.FAILED);
            sink.emit("error", Map.of("message", safeMessage(ex)));
        } finally {
            sink.complete();
        }
    }

    @Transactional
    protected ChatMessage saveUserAndAssistantPlaceholder(Long sessionId, Long modelId, String content) {
        int maxSeq = chatMessageRepository.findMaxSeqNoBySessionId(sessionId);
        LocalDateTime now = LocalDateTime.now();

        ChatMessage userMessage = new ChatMessage();
        userMessage.setSessionId(sessionId);
        userMessage.setRole(MessageRole.USER);
        userMessage.setStatus(MessageStatus.DONE);
        userMessage.setContent(content);
        userMessage.setSeqNo(maxSeq + 1);
        userMessage.setModelId(modelId);
        userMessage.setCreatedAt(now);
        chatMessageRepository.insert(userMessage);

        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setSessionId(sessionId);
        assistantMessage.setRole(MessageRole.ASSISTANT);
        assistantMessage.setStatus(MessageStatus.STREAMING);
        assistantMessage.setContent("");
        assistantMessage.setSeqNo(maxSeq + 2);
        assistantMessage.setModelId(modelId);
        assistantMessage.setCreatedAt(now);
        chatMessageRepository.insert(assistantMessage);

        ChatSession session = requireSession(sessionId);
        session.setLastModelId(modelId);
        session.touch();
        chatSessionRepository.updateById(session);
        return assistantMessage;
    }

    @Transactional
    protected void finalizeAssistantMessage(Long assistantMessageId, String content, MessageStatus status) {
        ChatMessage assistant = chatMessageRepository.selectById(assistantMessageId);
        if (assistant == null) {
            throw new NotFoundException("Assistant message not found");
        }
        assistant.setContent(content == null ? "" : content);
        assistant.setStatus(status);
        chatMessageRepository.updateById(assistant);
    }

    @Transactional
    protected void updateSessionAfterChat(Long sessionId, Long modelId) {
        ChatSession session = requireSession(sessionId);
        session.setLastModelId(modelId);
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionRepository.updateById(session);
    }

    protected ChatSession requireSession(Long sessionId) {
        LambdaQueryWrapper<ChatSession> query = new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getId, sessionId)
                .eq(ChatSession::isDeleted, false)
                .last("LIMIT 1");
        ChatSession session = chatSessionRepository.selectOne(query);
        if (session == null) {
            throw new NotFoundException("Session not found");
        }
        return session;
    }

    protected AiModelProviderView requireModel(Long modelId) {
        AiModelProviderView model = aiModelRepository.findAvailableById(modelId);
        if (model == null) {
            throw new NotFoundException("Model not found or disabled");
        }
        return model;
    }

    private String safeMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return "Unknown error";
        }
        if (message.length() > 300) {
            return message.substring(0, 300);
        }
        return message;
    }
}
