package com.example.copilot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.copilot.dto.request.CreateKnowledgeBaseRequest;
import com.example.copilot.dto.response.KnowledgeBaseResponse;
import com.example.copilot.entity.Document;
import com.example.copilot.entity.KnowledgeBase;
import com.example.copilot.exception.ForbiddenException;
import com.example.copilot.exception.NotFoundException;
import com.example.copilot.mapper.DocumentMapper;
import com.example.copilot.mapper.KnowledgeBaseMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final DocumentMapper documentMapper;

    public KnowledgeBaseServiceImpl(KnowledgeBaseMapper knowledgeBaseMapper, DocumentMapper documentMapper) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.documentMapper = documentMapper;
    }

    @Override
    public KnowledgeBaseResponse create(Long ownerId, CreateKnowledgeBaseRequest request) {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setOwnerId(ownerId);
        kb.setName(request.name());
        kb.setDescription(request.description());
        kb.setCreatedAt(LocalDateTime.now());
        knowledgeBaseMapper.insert(kb);
        return toResponse(kb);
    }

    @Override
    public List<KnowledgeBaseResponse> listByOwner(Long ownerId) {
        return knowledgeBaseMapper.selectList(new LambdaQueryWrapper<KnowledgeBase>()
                        .eq(KnowledgeBase::getOwnerId, ownerId)
                        .orderByDesc(KnowledgeBase::getCreatedAt))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public KnowledgeBaseResponse getOwned(Long id, Long ownerId) {
        KnowledgeBase kb = knowledgeBaseMapper.selectById(id);
        if (kb == null) {
            throw new NotFoundException("知识库不存在");
        }
        if (!kb.getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("无权访问该知识库");
        }
        return toResponse(kb);
    }

    @Override
    public void deleteOwned(Long id, Long ownerId) {
        KnowledgeBase kb = knowledgeBaseMapper.selectById(id);
        if (kb == null) {
            throw new NotFoundException("知识库不存在");
        }
        if (!kb.getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("无权删除该知识库");
        }

        List<Document> documents = documentMapper.selectList(new LambdaQueryWrapper<Document>()
                .eq(Document::getKnowledgeBaseId, id));
        for (Document document : documents) {
            deleteStoredFileQuietly(document.getStoragePath());
        }

        knowledgeBaseMapper.deleteById(id);
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

    private KnowledgeBaseResponse toResponse(KnowledgeBase kb) {
        return new KnowledgeBaseResponse(kb.getId(), kb.getName(), kb.getDescription(), kb.getCreatedAt());
    }
}
