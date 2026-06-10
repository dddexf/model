package com.example.chatapp.model.req;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SessionCreateRequest {

    @Size(max = 120, message = "title length must be <= 120")
    private String title;
}
