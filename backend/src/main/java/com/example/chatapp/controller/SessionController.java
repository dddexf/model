package com.example.chatapp.controller;

import com.example.chatapp.model.dto.ChatMessageDto;
import com.example.chatapp.model.req.SessionCreateRequest;
import com.example.chatapp.model.dto.SessionDto;
import com.example.chatapp.model.req.SessionRenameRequest;
import com.example.chatapp.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionDto createSession(@Valid @RequestBody(required = false) SessionCreateRequest request) {
        String title = request == null ? null : request.getTitle();
        return sessionService.createSession(title);
    }

    @GetMapping
    public List<SessionDto> listSessions() {
        return sessionService.listSessions();
    }

    @PatchMapping("/{sessionId}/title")
    public SessionDto renameSession(@PathVariable Long sessionId, @Valid @RequestBody SessionRenameRequest request) {
        return sessionService.renameSession(sessionId, request.getTitle());
    }

    @DeleteMapping("/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(@PathVariable Long sessionId) {
        sessionService.deleteSession(sessionId);
    }

    @GetMapping("/{sessionId}/messages")
    public List<ChatMessageDto> listMessages(@PathVariable Long sessionId) {
        return sessionService.listMessages(sessionId);
    }
}
