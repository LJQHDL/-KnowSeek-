package com.example.copilot.dto.response;

import java.time.LocalDateTime;

public record KnowledgeBaseResponse(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt
) {
}
