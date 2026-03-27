package com.example.chatapp.service;

import com.example.chatapp.dto.ModelOptionDto;
import com.example.chatapp.repository.AiModelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModelService {

    private final AiModelRepository aiModelRepository;

    public ModelService(AiModelRepository aiModelRepository) {
        this.aiModelRepository = aiModelRepository;
    }

    public List<ModelOptionDto> listAvailableModels() {
        return aiModelRepository.findAllAvailable()
                .stream()
                .map(item -> new ModelOptionDto(
                        item.getId(),
                        item.getDisplayName(),
                        item.getModelCode(),
                        item.getProviderName()
                ))
                .toList();
    }
}
