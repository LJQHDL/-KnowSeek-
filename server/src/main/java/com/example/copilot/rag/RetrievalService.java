package com.example.copilot.rag;

import com.example.copilot.dto.response.RetrievalResult;

public interface RetrievalService {

    RetrievalResult retrieveTopChunks(Long sessionId, String query, int topK);

    RetrievalResult retrieveByKnowledgeBaseId(Long knowledgeBaseId, String query, int topK);
}
