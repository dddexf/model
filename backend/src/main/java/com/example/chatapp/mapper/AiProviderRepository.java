package com.example.chatapp.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.chatapp.model.entity.AiProvider;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiProviderRepository extends BaseMapper<AiProvider> {
}
