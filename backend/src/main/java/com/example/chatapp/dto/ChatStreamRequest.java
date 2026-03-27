package com.example.chatapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ChatStreamRequest {

    @NotNull(message = "sessionId is required")
    private Long sessionId;

    @NotNull(message = "modelId is required")
    private Long modelId;

    @NotBlank(message = "message cannot be blank")
    @Size(max = 8000, message = "message length must be <= 8000")
    private String message;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
