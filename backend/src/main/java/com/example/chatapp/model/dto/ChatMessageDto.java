package com.example.chatapp.dto;

import com.example.chatapp.enums.MessageRole;
import com.example.chatapp.enums.MessageStatus;

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
