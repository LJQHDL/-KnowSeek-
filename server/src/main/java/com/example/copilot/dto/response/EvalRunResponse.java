package com.example.copilot.dto.response;

import java.time.LocalDateTime;

public record EvalRunResponse(
        Long id,
        Long knowledgeBaseId,
        String name,
        String status,
        Integer totalCases,
        Integer hitCount,
        Double hitRate,
        Double mrr,
        Long avgLatencyMs,
        Double avgScore,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        LocalDateTime createdAt
) {
}
