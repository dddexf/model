package com.example.chatapp.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ai_model")
public class AiModel {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("provider_id")
    private Long providerId;

    @TableField("model_code")
    private String modelCode;

    @TableField("display_name")
    private String displayName;

    @TableField("max_context_tokens")
    private Integer maxContextTokens = 8000;

    @TableField("enabled")
    private boolean enabled = true;

    @TableField("sort_no")
    private Integer sortNo = 0;
}
