package com.example.copilot.service;

import com.example.copilot.dto.response.LlmAnswerResult;
import com.example.copilot.dto.response.RetrievedChunkResponse;

import java.util.List;

public interface LlmAnswerService {

    LlmAnswerResult generateAnswer(String userQuestion, List<RetrievedChunkResponse> chunks, String fallbackAnswer);
}
