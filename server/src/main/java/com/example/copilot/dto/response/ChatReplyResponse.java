package com.example.copilot.dto.response;

import java.util.List;

public record ChatReplyResponse(
        MessageResponse userMessage,
        MessageResponse assistantMessage,
        List<RetrievedChunkResponse> retrievedChunks
) {
}
