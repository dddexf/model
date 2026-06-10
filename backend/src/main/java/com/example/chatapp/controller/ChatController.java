package com.example.chatapp.controller;

import com.example.chatapp.model.req.ChatStreamRequest;
import com.example.chatapp.service.ChatService;
import com.example.chatapp.stream.SseStreamSink;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.Executor;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final Executor chatExecutor;

    public ChatController(ChatService chatService, @Qualifier("chatExecutor") Executor chatExecutor) {
        this.chatService = chatService;
        this.chatExecutor = chatExecutor;
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@Valid @RequestBody ChatStreamRequest request) {
        SseEmitter emitter = new SseEmitter(0L);
        chatExecutor.execute(() -> chatService.streamChat(request, new SseStreamSink(emitter)));
        return emitter;
    }
}
