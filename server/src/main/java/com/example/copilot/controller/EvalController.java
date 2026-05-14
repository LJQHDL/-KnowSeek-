package com.example.copilot.controller;

import com.example.copilot.common.ApiResponse;
import com.example.copilot.dto.request.CreateEvalRunRequest;
import com.example.copilot.dto.response.EvalRunResponse;
import com.example.copilot.service.EvaluationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/evals")
@PreAuthorize("hasRole('ADMIN')")
public class EvalController {

    private final EvaluationService evaluationService;

    public EvalController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping("/run")
    public ApiResponse<EvalRunResponse> createAndRun(@Valid @RequestBody CreateEvalRunRequest request) {
        return ApiResponse.success(evaluationService.createAndRun(
                request.knowledgeBaseId(), request.name(), request.cases()));
    }

    @GetMapping
    public ApiResponse<List<EvalRunResponse>> listRuns() {
        return ApiResponse.success(evaluationService.listRuns());
    }

    @GetMapping("/{id}")
    public ApiResponse<EvalRunResponse> getRunDetail(@PathVariable Long id) {
        return ApiResponse.success(evaluationService.getRunDetail(id));
    }
}
