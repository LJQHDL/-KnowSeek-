package com.example.copilot.dto.response;

import java.time.LocalDateTime;

public record LogMessageResponse(
        Long id,
        Long sessionId,
        Long knowledgeBaseId,
        String knowledgeBaseName,
        String role,
        String content,
        String citationsJson,
        Integer latencyMs,
        Integer promptTokens,
        Integer completionTokens,
        LocalDateTime createdAt
) {
}
