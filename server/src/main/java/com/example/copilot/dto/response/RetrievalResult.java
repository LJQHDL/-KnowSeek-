package com.example.copilot.dto.response;

import java.util.List;

public record RetrievalResult(
        List<RetrievedChunkResponse> chunks,
        int latencyMs
) {
}
