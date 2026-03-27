package com.example.chatapp.dto;

import com.example.chatapp.domain.enums.MessageRole;
import com.example.chatapp.domain.enums.MessageStatus;

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
