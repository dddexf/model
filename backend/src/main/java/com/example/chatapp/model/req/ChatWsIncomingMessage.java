package com.example.chatapp.model.req;

import lombok.Data;

@Data
public class ChatWsIncomingMessage {

    private String action;
    private Long sessionId;
    private Long modelId;
    private String message;
}
