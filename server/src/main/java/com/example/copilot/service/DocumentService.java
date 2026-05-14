package com.example.copilot.service;

import com.example.copilot.dto.response.DocumentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DocumentService {

    DocumentResponse upload(Long userId, Long knowledgeBaseId, MultipartFile file) throws IOException;

    List<DocumentResponse> list(Long userId, Long knowledgeBaseId);

    DocumentResponse getStatus(Long userId, Long documentId);

    void delete(Long userId, Long documentId);
}
