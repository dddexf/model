package com.example.chatapp.model.dto;

import com.example.chatapp.model.enums.MessageRole;
import com.example.chatapp.model.enums.MessageStatus;

import java.time.LocalDateTime;

public record ChatMessageDto(
        Long id,
        MessageRole role,
        String content,
        MessageStatus status,
        Integer seqNo,
        LocalDateTime createdAt
) {
}
