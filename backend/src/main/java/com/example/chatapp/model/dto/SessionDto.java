package com.example.chatapp.model.dto;

import java.time.LocalDateTime;

public record SessionDto(
        Long id,
        String title,
        LocalDateTime updatedAt
) {
}
