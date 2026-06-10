package com.example.chatapp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.chatapp.model.enums.MessageRole;
import com.example.chatapp.model.enums.MessageStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("session_id")
    private Long sessionId;

    @TableField("role")
    private MessageRole role;

    @TableField("content")
    private String content;

    @TableField("seq_no")
    private Integer seqNo;

    @TableField("model_id")
    private Long modelId;

    @TableField("status")
    private MessageStatus status;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
