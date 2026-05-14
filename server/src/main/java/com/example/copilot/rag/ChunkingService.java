package com.example.copilot.rag;

import java.util.List;

public interface ChunkingService {

    List<String> split(String content);
}
