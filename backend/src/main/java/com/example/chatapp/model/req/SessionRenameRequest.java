package com.example.chatapp.model.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SessionRenameRequest {

    @NotBlank(message = "title cannot be blank")
    @Size(max = 120, message = "title length must be <= 120")
    private String title;
}
