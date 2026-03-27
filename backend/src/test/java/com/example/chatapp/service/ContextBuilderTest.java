package com.example.chatapp.service;

import com.example.chatapp.domain.entity.ChatMessage;
import com.example.chatapp.domain.enums.MessageRole;
import com.example.chatapp.domain.enums.MessageStatus;
import com.example.chatapp.service.client.LlmMessage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContextBuilderTest {

    private final ContextBuilder contextBuilder = new ContextBuilder(new TokenEstimator());

    @Test
    void shouldKeepRecentRounds() {
        List<ChatMessage> history = buildHistory(8);

        List<LlmMessage> result = contextBuilder.build(history, 10000, 2);

        assertEquals(5, result.size());
        assertEquals("msg-4", result.get(0).content());
        assertEquals("msg-8", result.get(4).content());
    }

    @Test
    void shouldTrimByTokenLimit() {
        List<ChatMessage> history = new ArrayList<>();
        history.add(message(1, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
        history.add(message(2, "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"));
        history.add(message(3, "cccccccccccccccccccccccccccccccc"));

        List<LlmMessage> result = contextBuilder.build(history, 24, 10);

        assertEquals(1, result.size());
        assertTrue(result.get(0).content().startsWith("c"));
    }

    private List<ChatMessage> buildHistory(int count) {
        List<ChatMessage> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(message(i, "msg-" + i));
        }
        return list;
    }

    private ChatMessage message(int seqNo, String content) {
        ChatMessage message = new ChatMessage();
        message.setSeqNo(seqNo);
        message.setRole(seqNo % 2 == 0 ? MessageRole.ASSISTANT : MessageRole.USER);
        message.setStatus(MessageStatus.DONE);
        message.setContent(content);
        return message;
    }
}
