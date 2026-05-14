package com.example.copilot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.copilot.common.DocumentStatusEnum;
import com.example.copilot.common.DocumentTypeEnum;
import com.example.copilot.dto.response.DocumentResponse;
import com.example.copilot.entity.Document;
import com.example.copilot.entity.KnowledgeBase;
import com.example.copilot.exception.ForbiddenException;
import com.example.copilot.exception.NotFoundException;
import com.example.copilot.mapper.DocumentMapper;
import com.example.copilot.mapper.KnowledgeBaseMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private static final Path STORAGE_ROOT = Paths.get("storage", "uploads");

    private final DocumentMapper documentMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final DocumentProcessingService documentProcessingService;

    public DocumentService(DocumentMapper documentMapper,
                           KnowledgeBaseMapper knowledgeBaseMapper,
                           DocumentProcessingService documentProcessingService) {
        this.documentMapper = documentMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.documentProcessingService = documentProcessingService;
    }

    public DocumentResponse upload(Long userId, Long knowledgeBaseId, MultipartFile file) throws IOException {
        KnowledgeBase kb = knowledgeBaseMapper.selectById(knowledgeBaseId);
        if (kb == null) {
            throw new NotFoundException("知识库不存在");
        }
        if (!kb.getOwnerId().equals(userId)) {
            throw new ForbiddenException("无权向该知识库上传文档");
        }
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        Files.createDirectories(STORAGE_ROOT);
        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path storedPath = STORAGE_ROOT.resolve(storedName);
        file.transferTo(storedPath);

        Document document = new Document();
        document.setKnowledgeBaseId(knowledgeBaseId);
        document.setFileName(file.getOriginalFilename());
        document.setFileType(detectFileType(file.getOriginalFilename()));
        document.setStoragePath(storedPath.toString());
        document.setStatus(DocumentStatusEnum.UPLOADED.name());
        document.setCreatedAt(LocalDateTime.now());
        documentMapper.insert(document);
        documentProcessingService.processDocumentAsync(document.getId());
        return toResponse(document);
    }

    public List<DocumentResponse> list(Long userId, Long knowledgeBaseId) {
        ensureOwnership(userId, knowledgeBaseId);
        return documentMapper.selectList(new LambdaQueryWrapper<Document>()
                        .eq(Document::getKnowledgeBaseId, knowledgeBaseId)
                        .orderByDesc(Document::getCreatedAt))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public DocumentResponse getStatus(Long userId, Long documentId) {
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new NotFoundException("文档不存在");
        }
        ensureOwnership(userId, document.getKnowledgeBaseId());
        return toResponse(document);
    }

    public void delete(Long userId, Long documentId) {
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new NotFoundException("文档不存在");
        }
        ensureOwnership(userId, document.getKnowledgeBaseId());
        deleteStoredFileQuietly(document.getStoragePath());
        documentMapper.deleteById(documentId);
    }

    private void ensureOwnership(Long userId, Long knowledgeBaseId) {
        KnowledgeBase kb = knowledgeBaseMapper.selectById(knowledgeBaseId);
        if (kb == null) {
            throw new NotFoundException("知识库不存在");
        }
        if (!kb.getOwnerId().equals(userId)) {
            throw new ForbiddenException("无权访问该知识库");
        }
    }

    private String detectFileType(String fileName) {
        String lower = fileName == null ? "" : fileName.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return DocumentTypeEnum.PDF.name();
        }
        if (lower.endsWith(".docx")) {
            return DocumentTypeEnum.DOCX.name();
        }
        if (lower.endsWith(".md") || lower.endsWith(".markdown")) {
            return DocumentTypeEnum.MARKDOWN.name();
        }
        return DocumentTypeEnum.UNKNOWN.name();
    }

    private void deleteStoredFileQuietly(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(Path.of(storagePath));
        } catch (IOException ignored) {
        }
    }

    private DocumentResponse toResponse(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getKnowledgeBaseId(),
                document.getFileName(),
                document.getFileType(),
                document.getStatus(),
                document.getErrorMessage(),
                document.getCreatedAt()
        );
    }
}
