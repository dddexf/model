package com.example.chatapp.stream;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

public class SseStreamSink implements StreamSink {

    private final SseEmitter emitter;

    public SseStreamSink(SseEmitter emitter) {
        this.emitter = emitter;
    }

    @Override
    public void emit(String event, Object data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
        } catch (IOException e) {
            throw new RuntimeException("SSE connection broken", e);
        }
    }

    @Override
    public void complete() {
        emitter.complete();
    }
}
