package com.example.copilot.rag.impl;

import com.example.copilot.rag.RetrievalService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.copilot.entity.ChatSession;
import com.example.copilot.entity.DocumentChunk;
import com.example.copilot.dto.response.RetrievalResult;
import com.example.copilot.dto.response.RetrievedChunkResponse;
import com.example.copilot.exception.NotFoundException;
import com.example.copilot.mapper.ChatSessionMapper;
import com.example.copilot.mapper.DocumentChunkMapper;
import com.example.copilot.service.EmbeddingService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class RetrievalServiceImpl implements RetrievalService {

    private static final int MAX_SNIPPET_LENGTH = 1200;

    private final ChatSessionMapper chatSessionMapper;
    private final DocumentChunkMapper documentChunkMapper;
    private final EmbeddingService embeddingService;

    public RetrievalServiceImpl(ChatSessionMapper chatSessionMapper,
                                DocumentChunkMapper documentChunkMapper,
                                EmbeddingService embeddingService) {
        this.chatSessionMapper = chatSessionMapper;
        this.documentChunkMapper = documentChunkMapper;
        this.embeddingService = embeddingService;
    }

    @Override
    public RetrievalResult retrieveTopChunks(Long sessionId, String query, int topK) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new NotFoundException("会话不存在");
        }
        return retrieveByKnowledgeBaseId(session.getKnowledgeBaseId(), query, topK);
    }

    @Override
    public RetrievalResult retrieveByKnowledgeBaseId(Long knowledgeBaseId, String query, int topK) {
        long start = System.currentTimeMillis();

        String normalizedQuery = query == null ? "" : query.toLowerCase(Locale.ROOT);
        List<DocumentChunk> candidates = vectorSearch(knowledgeBaseId, normalizedQuery, topK);
        if (candidates.isEmpty()) {
            candidates = documentChunkMapper.selectList(new LambdaQueryWrapper<DocumentChunk>()
                    .eq(DocumentChunk::getKnowledgeBaseId, knowledgeBaseId)
                    .orderByAsc(DocumentChunk::getChunkIndex));
        }

        List<RetrievedChunkResponse> chunks = candidates.stream()
                .sorted(Comparator.comparingInt((DocumentChunk chunk) -> score(normalizedQuery, chunk.getContent())).reversed())
                .limit(topK)
                .map(chunk -> new RetrievedChunkResponse(
                        chunk.getId(),
                        chunk.getDocumentId(),
                        chunk.getChunkIndex(),
                        abbreviate(chunk.getContent())
                ))
                .toList();
        int latency = (int) (System.currentTimeMillis() - start);
        return new RetrievalResult(chunks, latency);
    }

    private List<DocumentChunk> vectorSearch(Long knowledgeBaseId, String query, int topK) {
        float[] embedding = embeddingService.embed(query);
        if (embedding.length == 0) {
            return List.of();
        }
        String vectorLiteral = EmbeddingService.toPgVectorLiteral(embedding);
        try {
            return documentChunkMapper.selectTopKByEmbedding(knowledgeBaseId, vectorLiteral, topK);
        } catch (Exception ex) {
            return List.of();
        }
    }

    private int score(String query, String content) {
        if (query.isBlank() || content == null || content.isBlank()) {
            return 0;
        }
        int score = 0;
        String lowerContent = content.toLowerCase(Locale.ROOT);
        for (String token : query.split("\\s+")) {
            if (!token.isBlank() && lowerContent.contains(token)) {
                score += 2;
            }
        }
        if (lowerContent.length() > 0) {
            score += 1;
        }
        return score;
    }

    private String abbreviate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= MAX_SNIPPET_LENGTH ? text : text.substring(0, MAX_SNIPPET_LENGTH) + "...";
    }
}
