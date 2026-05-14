package com.example.copilot.service;

import com.example.copilot.common.DocumentStatusEnum;
import com.example.copilot.entity.Document;
import com.example.copilot.entity.DocumentChunk;
import com.example.copilot.mapper.DocumentChunkMapper;
import com.example.copilot.mapper.DocumentMapper;
import com.example.copilot.rag.ChunkingService;
import com.example.copilot.rag.parser.DocumentParser;
import com.example.copilot.rag.parser.DocumentParserFactory;
import com.example.copilot.rag.parser.ParsedDocument;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentProcessingService {

    private final DocumentMapper documentMapper;
    private final DocumentChunkMapper documentChunkMapper;
    private final DocumentParserFactory documentParserFactory;
    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;

    public DocumentProcessingService(DocumentMapper documentMapper,
                                     DocumentChunkMapper documentChunkMapper,
                                     DocumentParserFactory documentParserFactory,
                                     ChunkingService chunkingService,
                                     EmbeddingService embeddingService) {
        this.documentMapper = documentMapper;
        this.documentChunkMapper = documentChunkMapper;
        this.documentParserFactory = documentParserFactory;
        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
    }

    @Async("documentProcessingExecutor")
    public void processDocumentAsync(Long documentId) {
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            return;
        }

        try {
            updateStatus(document, DocumentStatusEnum.PARSING.name(), null);
            DocumentParser parser = documentParserFactory.getParser(document.getFileType());
            ParsedDocument parsedDocument = parser.parse(Path.of(document.getStoragePath()));

            updateStatus(document, DocumentStatusEnum.INDEXING.name(), null);
            List<String> chunks = chunkingService.split(parsedDocument.content());
            List<float[]> embeddings = embeddingService.embed(chunks);
            saveChunks(document, chunks, embeddings, parsedDocument.metadataJson());

            updateStatus(document, DocumentStatusEnum.READY.name(), null);
        } catch (Exception ex) {
            updateStatus(document, DocumentStatusEnum.FAILED.name(), ex.getMessage());
        }
    }

    private void saveChunks(Document document, List<String> chunks, List<float[]> embeddings, String metadataJson) {
        if (chunks.isEmpty()) {
            throw new IllegalStateException("文档解析结果为空，无法切分");
        }
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk chunk = new DocumentChunk();
            chunk.setDocumentId(document.getId());
            chunk.setKnowledgeBaseId(document.getKnowledgeBaseId());
            chunk.setChunkIndex(i);
            chunk.setContent(chunks.get(i));
            chunk.setTokenCount(estimateTokenCount(chunks.get(i)));
            if (embeddings.size() > i && embeddings.get(i) != null && embeddings.get(i).length > 0) {
                chunk.setEmbedding(EmbeddingService.toPgVectorLiteral(embeddings.get(i)));
            }
            chunk.setMetadataJson(metadataJson);
            chunk.setCreatedAt(LocalDateTime.now());
            if (chunk.getEmbedding() != null && !chunk.getEmbedding().isBlank()) {
                documentChunkMapper.insertWithEmbedding(chunk);
            } else {
                documentChunkMapper.insert(chunk);
            }
        }
    }

    private int estimateTokenCount(String chunk) {
        return Math.max(1, chunk.length() / 4);
    }

    private void updateStatus(Document document, String status, String errorMessage) {
        document.setStatus(status);
        document.setErrorMessage(errorMessage);
        documentMapper.updateById(document);
    }
}
