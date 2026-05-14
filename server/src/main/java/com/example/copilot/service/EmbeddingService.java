package com.example.copilot.service;

import java.util.List;

public interface EmbeddingService {

    List<float[]> embed(List<String> texts);

    float[] embed(String text);

    static String toPgVectorLiteral(float[] embedding) {
        if (embedding == null || embedding.length == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(embedding[i]);
        }
        builder.append(']');
        return builder.toString();
    }
}
