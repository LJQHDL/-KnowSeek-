package com.example.copilot.dto.response;

import java.time.LocalDateTime;

public record ChatSessionResponse(
        Long id,
        Long knowledgeBaseId,
        String title,
        LocalDateTime createdAt
) {
}
