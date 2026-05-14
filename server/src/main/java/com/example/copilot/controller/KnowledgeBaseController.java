package com.example.copilot.controller;

import com.example.copilot.common.ApiResponse;
import com.example.copilot.dto.request.CreateKnowledgeBaseRequest;
import com.example.copilot.dto.response.KnowledgeBaseResponse;
import com.example.copilot.security.UserPrincipal;
import com.example.copilot.service.KnowledgeBaseService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge-bases")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @PostMapping
    public ApiResponse<KnowledgeBaseResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                     @Valid @RequestBody CreateKnowledgeBaseRequest request) {
        return ApiResponse.success(knowledgeBaseService.create(principal.getId(), request));
    }

    @GetMapping
    public ApiResponse<List<KnowledgeBaseResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(knowledgeBaseService.listByOwner(principal.getId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<KnowledgeBaseResponse> detail(@AuthenticationPrincipal UserPrincipal principal,
                                                     @PathVariable Long id) {
        return ApiResponse.success(knowledgeBaseService.getOwned(id, principal.getId()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal UserPrincipal principal,
                                    @PathVariable Long id) {
        knowledgeBaseService.deleteOwned(id, principal.getId());
        return ApiResponse.success();
    }
}
