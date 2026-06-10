package com.example.chatapp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.chatapp.model.entity.AiModel;
import com.example.chatapp.repository.projection.AiModelProviderView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AiModelRepository extends BaseMapper<AiModel> {

    @Select("""
            SELECT m.id,
                   m.provider_id AS providerId,
                   m.model_code AS modelCode,
                   m.display_name AS displayName,
                   m.max_context_tokens AS maxContextTokens,
                   m.sort_no AS sortNo,
                   p.name AS providerName,
                   p.base_url AS providerBaseUrl,
                   p.api_key_cipher AS providerApiKeyCipher
            FROM ai_model m
            JOIN ai_provider p ON p.id = m.provider_id
            WHERE m.enabled = 1 AND p.enabled = 1
            ORDER BY m.sort_no ASC, m.id ASC
            """)
    List<AiModelProviderView> findAllAvailable();

    @Select("""
            SELECT m.id,
                   m.provider_id AS providerId,
                   m.model_code AS modelCode,
                   m.display_name AS displayName,
                   m.max_context_tokens AS maxContextTokens,
                   m.sort_no AS sortNo,
                   p.name AS providerName,
                   p.base_url AS providerBaseUrl,
                   p.api_key_cipher AS providerApiKeyCipher
            FROM ai_model m
            JOIN ai_provider p ON p.id = m.provider_id
            WHERE m.id = #{id} AND m.enabled = 1 AND p.enabled = 1
            """)
    AiModelProviderView findAvailableById(@Param("id") Long id);
}
