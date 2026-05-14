package com.example.copilot.dto.response;

public record EvalCaseResponse(
        Long id,
        String question,
        String expectedAnswer,
        String expectedSources,
        String actualAnswer,
        String actualSources,
        Boolean retrievalHit,
        Integer retrievalRank,
        Double score,
        Long latencyMs,
        String errorMessage
) {
}
