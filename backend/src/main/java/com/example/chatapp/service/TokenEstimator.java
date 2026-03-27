package com.example.chatapp.service;

import org.springframework.stereotype.Component;

@Component
public class TokenEstimator {

    public int estimate(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        // A practical approximation for mixed Chinese/English text.
        return Math.max(1, text.length() / 2);
    }
}
