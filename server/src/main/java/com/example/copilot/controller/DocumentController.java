package com.example.copilot.controller;

import com.example.copilot.common.ApiResponse;
import com.example.copilot.dto.response.DocumentResponse;
import com.example.copilot.security.UserPrincipal;
import com.example.copilot.service.DocumentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/knowledge-bases/{id}/documents")
    public ApiResponse<DocumentResponse> upload(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable Long id,
                                                @RequestParam("file") MultipartFile file) throws IOException {
        return ApiResponse.success(documentService.upload(principal.getId(), id, file));
    }

    @GetMapping("/knowledge-bases/{id}/documents")
    public ApiResponse<List<DocumentResponse>> list(@AuthenticationPrincipal UserPrincipal principal,
                                                    @PathVariable Long id) {
        return ApiResponse.success(documentService.list(principal.getId(), id));
    }

    @GetMapping("/documents/{id}/status")
    public ApiResponse<DocumentResponse> status(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable Long id) {
        return ApiResponse.success(documentService.getStatus(principal.getId(), id));
    }

    @DeleteMapping("/documents/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal UserPrincipal principal,
                                    @PathVariable Long id) {
        documentService.delete(principal.getId(), id);
        return ApiResponse.success();
    }

    @PostMapping("/documents/{id}/reindex")
    public ApiResponse<Void> reindex(@AuthenticationPrincipal UserPrincipal principal,
                                      @PathVariable Long id) {
        documentService.reindex(principal.getId(), id);
        return ApiResponse.success();
    }
}
