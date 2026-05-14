package com.example.copilot.service;

import com.example.copilot.dto.request.CreateEvalRunRequest;
import com.example.copilot.dto.response.EvalRunResponse;

import java.util.List;

public interface EvaluationService {

    EvalRunResponse createAndRun(Long knowledgeBaseId, String name, List<CreateEvalRunRequest.EvalCaseRequest> caseRequests);

    List<EvalRunResponse> listRuns();

    EvalRunResponse getRunDetail(Long runId);
}
