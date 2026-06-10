package com.example.chatapp.model.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatStreamRequest {

    @NotNull(message = "sessionId is required")
    private Long sessionId;

    @NotNull(message = "modelId is required")
    private Long modelId;

    @NotBlank(message = "message cannot be blank")
    @Size(max = 8000, message = "message length must be <= 8000")
    private String message;
}
