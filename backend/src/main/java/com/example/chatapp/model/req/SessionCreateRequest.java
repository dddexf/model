package com.example.chatapp.model.dto;

import jakarta.validation.constraints.Size;

public class SessionCreateRequest {

    @Size(max = 120, message = "title length must be <= 120")
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
