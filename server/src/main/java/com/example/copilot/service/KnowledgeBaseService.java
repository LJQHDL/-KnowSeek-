package com.example.copilot.service;

import com.example.copilot.dto.request.CreateKnowledgeBaseRequest;
import com.example.copilot.dto.response.KnowledgeBaseResponse;

import java.util.List;

public interface KnowledgeBaseService {

    KnowledgeBaseResponse create(Long ownerId, CreateKnowledgeBaseRequest request);

    List<KnowledgeBaseResponse> listByOwner(Long ownerId);

    KnowledgeBaseResponse getOwned(Long id, Long ownerId);

    void deleteOwned(Long id, Long ownerId);
}
