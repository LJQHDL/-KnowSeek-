package com.example.copilot.service.impl;

import com.example.copilot.service.EmbeddingService;

import com.example.copilot.config.AiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class EmbeddingServiceImpl implements EmbeddingService {

    private static final int DASHSCOPE_MAX_BATCH_SIZE = 10;

    private final ObjectProvider<EmbeddingModel> embeddingModelProvider;
    private final AiProperties aiProperties;

    public EmbeddingServiceImpl(ObjectProvider<EmbeddingModel> embeddingModelProvider, AiProperties aiProperties) {
        this.embeddingModelProvider = embeddingModelProvider;
        this.aiProperties = aiProperties;
    }

    @Override
    public List<float[]> embed(List<String> texts) {
        if (!aiProperties.isEnabled() || texts == null || texts.isEmpty()) {
            return List.of();
        }
        EmbeddingModel embeddingModel = embeddingModelProvider.getIfAvailable();
        if (embeddingModel == null) {
            return List.of();
        }

        int batchSize = Math.min(DASHSCOPE_MAX_BATCH_SIZE, Math.max(1, aiProperties.getEmbeddingBatchSize()));
        List<float[]> allEmbeddings = new ArrayList<>(texts.size());
        for (int start = 0; start < texts.size(); start += batchSize) {
            int end = Math.min(start + batchSize, texts.size());
            List<String> batch = texts.subList(start, end);
            try {
                allEmbeddings.addAll(embeddingModel.embed(batch));
            } catch (Exception ex) {
                log.warn("Embedding batch failed. start={}, end={}, size={}, message={}",
                        start,
                        end,
                        batch.size(),
                        ex.getMessage(),
                        ex);
                for (int i = 0; i < batch.size(); i++) {
                    allEmbeddings.add(new float[0]);
                }
            }
        }
        return allEmbeddings;
    }

    @Override
    public float[] embed(String text) {
        if (!aiProperties.isEnabled()) {
            return new float[0];
        }
        EmbeddingModel embeddingModel = embeddingModelProvider.getIfAvailable();
        if (embeddingModel == null) {
            return new float[0];
        }
        try {
            return embeddingModel.embed(text);
        } catch (Exception ex) {
            return new float[0];
        }
    }
}
