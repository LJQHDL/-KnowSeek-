package com.example.copilot.dto.response;

import java.time.LocalDateTime;

public record DocumentResponse(
        Long id,
        Long knowledgeBaseId,
        String fileName,
        String fileType,
        String status,
        String errorMessage,
        LocalDateTime createdAt
) {
}
