package com.example.chatapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SessionRenameRequest {

    @NotBlank(message = "title cannot be blank")
    @Size(max = 120, message = "title length must be <= 120")
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
