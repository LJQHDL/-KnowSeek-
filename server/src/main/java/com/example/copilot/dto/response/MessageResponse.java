package com.example.copilot.dto.response;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long sessionId,
        String role,
        String content,
        String citationsJson,
        Integer latencyMs,
        Integer promptTokens,
        Integer completionTokens,
        LocalDateTime createdAt
) {
}
