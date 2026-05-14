package com.example.copilot.dto.response;

public record LlmAnswerResult(String content, Integer promptTokens, Integer completionTokens) {
}
