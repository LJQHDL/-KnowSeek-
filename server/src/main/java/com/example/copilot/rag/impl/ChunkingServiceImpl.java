package com.example.copilot.rag.impl;

import com.example.copilot.rag.ChunkingService;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingServiceImpl implements ChunkingService {

    private static final int CHUNK_SIZE = 1000;

    @Override
    public List<String> split(String content) {
        String normalized = content == null ? "" : content.trim();
        if (normalized.isEmpty()) {
            return List.of();
        }

        List<String> chunks = new ArrayList<>();
        for (int start = 0; start < normalized.length(); start += CHUNK_SIZE) {
            int end = Math.min(start + CHUNK_SIZE, normalized.length());
            chunks.add(normalized.substring(start, end));
        }
        return chunks;
    }
}
