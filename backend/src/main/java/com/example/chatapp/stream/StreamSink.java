package com.example.chatapp.stream;

public interface StreamSink {

    void emit(String event, Object data);

    void complete();
}
