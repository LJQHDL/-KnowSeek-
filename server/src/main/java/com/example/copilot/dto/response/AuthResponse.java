package com.example.copilot.dto.response;

public record AuthResponse(
        Long userId,
        String email,
        String name,
        String token
) {
}
