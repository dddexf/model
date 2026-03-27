package com.example.chatapp.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.chatapp.domain.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatMessageRepository extends BaseMapper<ChatMessage> {

    @Select("""
            SELECT id, session_id, role, content, seq_no, model_id, status, created_at
            FROM chat_message
            WHERE session_id = #{sessionId}
            ORDER BY seq_no ASC
            """)
    List<ChatMessage> findBySessionIdOrderBySeqNoAsc(@Param("sessionId") Long sessionId);

    @Select("SELECT COALESCE(MAX(seq_no), 0) FROM chat_message WHERE session_id = #{sessionId}")
    int findMaxSeqNoBySessionId(@Param("sessionId") Long sessionId);
}
