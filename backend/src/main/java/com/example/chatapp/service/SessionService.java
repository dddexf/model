package com.example.chatapp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.chatapp.model.entity.ChatSession;
import com.example.chatapp.model.dto.ChatMessageDto;
import com.example.chatapp.model.dto.SessionDto;
import com.example.chatapp.exception.NotFoundException;
import com.example.chatapp.mapper.ChatMessageRepository;
import com.example.chatapp.mapper.ChatSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    public SessionService(ChatSessionRepository chatSessionRepository, ChatMessageRepository chatMessageRepository) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Transactional
    public SessionDto createSession(String title) {
        ChatSession session = new ChatSession();
        LocalDateTime now = LocalDateTime.now();
        session.setTitle((title == null || title.isBlank()) ? "New Session" : title.trim());
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        chatSessionRepository.insert(session);
        return toDto(session);
    }

    public List<SessionDto> listSessions() {
        LambdaQueryWrapper<ChatSession> query = new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::isDeleted, false)
                .orderByDesc(ChatSession::getUpdatedAt);
        return chatSessionRepository.selectList(query)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public SessionDto renameSession(Long sessionId, String title) {
        ChatSession session = requireSession(sessionId);
        session.setTitle(title.trim());
        session.touch();
        chatSessionRepository.updateById(session);
        return toDto(session);
    }

    @Transactional
    public void deleteSession(Long sessionId) {
        ChatSession session = requireSession(sessionId);
        session.setDeleted(true);
        session.touch();
        chatSessionRepository.updateById(session);
    }

    public List<ChatMessageDto> listMessages(Long sessionId) {
        requireSession(sessionId);
        return chatMessageRepository.findBySessionIdOrderBySeqNoAsc(sessionId)
                .stream()
                .map(message -> new ChatMessageDto(
                        message.getId(),
                        message.getRole(),
                        message.getContent(),
                        message.getStatus(),
                        message.getSeqNo(),
                        message.getCreatedAt()
                ))
                .toList();
    }

    public ChatSession requireSession(Long sessionId) {
        LambdaQueryWrapper<ChatSession> query = new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getId, sessionId)
                .eq(ChatSession::isDeleted, false)
                .last("LIMIT 1");
        ChatSession session = chatSessionRepository.selectOne(query);
        if (session == null) {
            throw new NotFoundException("Session not found");
        }
        return session;
    }

    private SessionDto toDto(ChatSession session) {
        return new SessionDto(session.getId(), session.getTitle(), session.getUpdatedAt());
    }
}
