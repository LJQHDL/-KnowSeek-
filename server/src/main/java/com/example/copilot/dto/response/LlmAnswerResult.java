package com.example.copilot.dto.response;

public record LlmAnswerResult(String content, int promptTokens, int completionTokens) {
}
