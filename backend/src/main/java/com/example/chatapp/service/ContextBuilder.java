package com.example.chatapp.service;

import com.example.chatapp.domain.entity.ChatMessage;
import com.example.chatapp.domain.enums.MessageRole;
import com.example.chatapp.service.client.LlmMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ContextBuilder {

    private final TokenEstimator tokenEstimator;

    public ContextBuilder(TokenEstimator tokenEstimator) {
        this.tokenEstimator = tokenEstimator;
    }

    public List<LlmMessage> build(List<ChatMessage> history, int maxContextTokens, int recentRounds) {
        int maxMessages = Math.max(3, recentRounds * 2 + 1);
        int startIndex = Math.max(0, history.size() - maxMessages);

        List<LlmMessage> result = new ArrayList<>();
        for (int i = startIndex; i < history.size(); i++) {
            ChatMessage message = history.get(i);
            if (message.getRole() == MessageRole.SYSTEM || message.getRole() == MessageRole.USER || message.getRole() == MessageRole.ASSISTANT) {
                result.add(new LlmMessage(message.getRole().name().toLowerCase(), message.getContent()));
            }
        }

        while (estimateTotalTokens(result) > maxContextTokens && result.size() > 1) {
            result.remove(0);
        }
        return result;
    }

    private int estimateTotalTokens(List<LlmMessage> messages) {
        int total = 0;
        for (LlmMessage message : messages) {
            total += tokenEstimator.estimate(message.content());
            total += 8; // role and structural overhead
        }
        return total;
    }
}
