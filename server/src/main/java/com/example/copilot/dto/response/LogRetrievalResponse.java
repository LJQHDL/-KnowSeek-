package com.example.copilot.dto.response;

import java.time.LocalDateTime;

public record LogRetrievalResponse(
        Long id,
        Long messageId,
        String queryText,
        String retrievedChunksJson,
        Integer topK,
        Integer latencyMs,
        LocalDateTime createdAt
) {
}
