package com.example.copilot.dto.response;

public record RetrievedChunkResponse(
        Long chunkId,
        Long documentId,
        Integer chunkIndex,
        String snippet
) {
}
