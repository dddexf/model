package com.example.chatapp.service;

import com.example.chatapp.domain.entity.AiModel;
import com.example.chatapp.domain.entity.AiProvider;
import com.example.chatapp.domain.entity.ChatMessage;
import com.example.chatapp.domain.entity.ChatSession;
import com.example.chatapp.domain.enums.MessageStatus;
import com.example.chatapp.dto.ChatStreamRequest;
import com.example.chatapp.repository.AiModelRepository;
import com.example.chatapp.repository.AiProviderRepository;
import com.example.chatapp.repository.ChatMessageRepository;
import com.example.chatapp.repository.ChatSessionRepository;
import com.example.chatapp.service.client.OpenAiCompatibleClient;
import com.example.chatapp.stream.StreamSink;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
class ChatServiceIntegrationTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private AesKeyCipher aesKeyCipher;

    @Autowired
    private AiProviderRepository aiProviderRepository;

    @Autowired
    private AiModelRepository aiModelRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @MockBean
    private OpenAiCompatibleClient openAiCompatibleClient;

    private AiModel model;
    private ChatSession session;

    @BeforeEach
    void setUp() {
        chatMessageRepository.delete(null);
        chatSessionRepository.delete(null);
        aiModelRepository.delete(null);
        aiProviderRepository.delete(null);

        LocalDateTime now = LocalDateTime.now();

        AiProvider provider = new AiProvider();
        provider.setName("deepseek");
        provider.setBaseUrl("https://api.deepseek.com/v1");
        provider.setApiKeyCipher(aesKeyCipher.encrypt("test-key"));
        provider.setEnabled(true);
        provider.setCreatedAt(now);
        provider.setUpdatedAt(now);
        aiProviderRepository.insert(provider);

        model = new AiModel();
        model.setProviderId(provider.getId());
        model.setDisplayName("DeepSeek Chat");
        model.setModelCode("deepseek-chat");
        model.setMaxContextTokens(2048);
        model.setEnabled(true);
        model.setSortNo(1);
        aiModelRepository.insert(model);

        session = new ChatSession();
        session.setTitle("test");
        session.setDeleted(false);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        chatSessionRepository.insert(session);
    }

    @Test
    void shouldEmitStartDeltaDoneAndPersistDoneMessage() {
        doAnswer(invocation -> {
            Consumer<String> onDelta = invocation.getArgument(4);
            onDelta.accept("A");
            onDelta.accept("B");
            return null;
        }).when(openAiCompatibleClient).streamChat(anyString(), anyString(), anyString(), anyList(), any());

        ChatStreamRequest request = new ChatStreamRequest();
        request.setSessionId(session.getId());
        request.setModelId(model.getId());
        request.setMessage("hello");

        InMemoryStreamSink sink = new InMemoryStreamSink();
        chatService.streamChat(request, sink);

        assertEquals(List.of("start", "delta", "delta", "done"), sink.eventNames());
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderBySeqNoAsc(session.getId());
        assertEquals(2, messages.size());
        assertEquals(MessageStatus.DONE, messages.get(1).getStatus());
        assertEquals("AB", messages.get(1).getContent());
    }

    @Test
    void shouldEmitErrorAndPersistFailedWhenUpstreamFails() {
        doThrow(new IllegalStateException("Upstream 401"))
                .when(openAiCompatibleClient).streamChat(anyString(), anyString(), anyString(), anyList(), any());

        ChatStreamRequest request = new ChatStreamRequest();
        request.setSessionId(session.getId());
        request.setModelId(model.getId());
        request.setMessage("hello");

        InMemoryStreamSink sink = new InMemoryStreamSink();
        chatService.streamChat(request, sink);

        assertEquals("start", sink.events.get(0).event);
        assertEquals("error", sink.events.get(1).event);
        assertTrue(((String) sink.events.get(1).data.get("message")).contains("Upstream 401"));

        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderBySeqNoAsc(session.getId());
        assertEquals(MessageStatus.FAILED, messages.get(1).getStatus());
    }

    private static class InMemoryStreamSink implements StreamSink {

        private final List<EventItem> events = new ArrayList<>();

        @Override
        @SuppressWarnings("unchecked")
        public void emit(String event, Object data) {
            events.add(new EventItem(event, (Map<String, Object>) data));
        }

        @Override
        public void complete() {
            // no-op
        }

        List<String> eventNames() {
            return events.stream().map(it -> it.event).toList();
        }
    }

    private record EventItem(String event, Map<String, Object> data) {
    }
}
