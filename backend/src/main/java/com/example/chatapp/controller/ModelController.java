package com.example.chatapp.controller;

import com.example.chatapp.model.dto.ModelOptionDto;
import com.example.chatapp.service.ModelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/models")
public class ModelController {

    private final ModelService modelService;

    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    @GetMapping
    public List<ModelOptionDto> listModels() {
        return modelService.listAvailableModels();
    }
}
