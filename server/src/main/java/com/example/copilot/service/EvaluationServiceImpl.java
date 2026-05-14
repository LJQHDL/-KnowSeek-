package com.example.copilot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.copilot.common.EvalStatusEnum;
import com.example.copilot.config.AiProperties;
import com.example.copilot.dto.request.CreateEvalRunRequest;
import com.example.copilot.dto.response.EvalCaseResponse;
import com.example.copilot.dto.response.EvalRunResponse;
import com.example.copilot.dto.response.LlmAnswerResult;
import com.example.copilot.dto.response.RetrievalResult;
import com.example.copilot.dto.response.RetrievedChunkResponse;
import com.example.copilot.entity.EvalCase;
import com.example.copilot.entity.EvalRun;
import com.example.copilot.exception.NotFoundException;
import com.example.copilot.mapper.EvalCaseMapper;
import com.example.copilot.mapper.EvalRunMapper;
import com.example.copilot.mapper.KnowledgeBaseMapper;
import com.example.copilot.rag.RetrievalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EvaluationServiceImpl implements EvaluationService {

    private final EvalRunMapper evalRunMapper;
    private final EvalCaseMapper evalCaseMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final RetrievalService retrievalService;
    private final LlmAnswerService llmAnswerService;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    public EvaluationServiceImpl(EvalRunMapper evalRunMapper,
                                 EvalCaseMapper evalCaseMapper,
                                 KnowledgeBaseMapper knowledgeBaseMapper,
                                 RetrievalService retrievalService,
                                 LlmAnswerService llmAnswerService,
                                 AiProperties aiProperties,
                                 ObjectMapper objectMapper) {
        this.evalRunMapper = evalRunMapper;
        this.evalCaseMapper = evalCaseMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.retrievalService = retrievalService;
        this.llmAnswerService = llmAnswerService;
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public EvalRunResponse createAndRun(Long knowledgeBaseId, String name, List<CreateEvalRunRequest.EvalCaseRequest> caseRequests) {
        if (knowledgeBaseMapper.selectById(knowledgeBaseId) == null) {
            throw new NotFoundException("知识库不存在");
        }

        EvalRun run = new EvalRun();
        run.setKnowledgeBaseId(knowledgeBaseId);
        run.setName(name);
        run.setStatus(EvalStatusEnum.PENDING.name());
        run.setTotalCases(caseRequests.size());
        run.setHitCount(0);
        run.setCreatedAt(LocalDateTime.now());
        evalRunMapper.insert(run);

        List<EvalCase> cases = new ArrayList<>();
        for (CreateEvalRunRequest.EvalCaseRequest req : caseRequests) {
            EvalCase evalCase = new EvalCase();
            evalCase.setEvalRunId(run.getId());
            evalCase.setQuestion(req.question());
            evalCase.setExpectedAnswer(req.expectedAnswer());
            evalCase.setExpectedSources(req.expectedSources());
            cases.add(evalCase);
        }
        for (EvalCase c : cases) {
            evalCaseMapper.insert(c);
        }

        executeRun(run, cases);
        return toResponse(run, cases);
    }

    @Override
    public List<EvalRunResponse> listRuns() {
        return evalRunMapper.selectList(new LambdaQueryWrapper<EvalRun>()
                        .orderByDesc(EvalRun::getCreatedAt))
                .stream()
                .map(r -> toResponse(r, List.of()))
                .toList();
    }

    @Override
    public EvalRunResponse getRunDetail(Long runId) {
        EvalRun run = evalRunMapper.selectById(runId);
        if (run == null) {
            throw new NotFoundException("评测运行不存在");
        }
        List<EvalCase> cases = evalCaseMapper.selectList(new LambdaQueryWrapper<EvalCase>()
                .eq(EvalCase::getEvalRunId, runId));
        return toResponse(run, cases);
    }

    private void executeRun(EvalRun run, List<EvalCase> cases) {
        run.setStatus(EvalStatusEnum.RUNNING.name());
        run.setStartedAt(LocalDateTime.now());
        evalRunMapper.updateById(run);

        int hitCount = 0;
        double mrrSum = 0;
        Long totalLatency = 0L;
        double totalScore = 0;
        int validCases = 0;

        for (EvalCase evalCase : cases) {
            long start = System.currentTimeMillis();
            try {
                RetrievalResult retrieval = retrievalService.retrieveByKnowledgeBaseId(
                        run.getKnowledgeBaseId(), evalCase.getQuestion(), aiProperties.getRetrievalTopK());

                boolean hit = checkHit(evalCase.getExpectedSources(), retrieval.chunks());
                int rank = findRank(evalCase.getExpectedSources(), retrieval.chunks());

                String fallback = "基于检索片段生成的临时回答";
                LlmAnswerResult llmResult = llmAnswerService.generateAnswer(
                        evalCase.getQuestion(), retrieval.chunks(), fallback);

                double score = calculateScore(evalCase.getExpectedAnswer(), llmResult.content());

                Long latency = System.currentTimeMillis() - start;

                String actualSourcesJson = toJson(retrieval.chunks());

                evalCase.setActualAnswer(llmResult.content());
                evalCase.setActualSources(actualSourcesJson);
                evalCase.setRetrievalHit(hit);
                evalCase.setRetrievalRank(rank);
                evalCase.setScore(score);
                evalCase.setLatencyMs(latency);
                evalCaseMapper.updateById(evalCase);

                if (hit) hitCount++;
                if (rank > 0) mrrSum += 1.0 / rank;
                totalLatency += latency;
                totalScore += score;
                validCases++;

            } catch (Exception e) {
                evalCase.setErrorMessage(e.getMessage());
                evalCase.setLatencyMs(System.currentTimeMillis() - start);
                evalCaseMapper.updateById(evalCase);
            }
        }

        run.setStatus(EvalStatusEnum.COMPLETED.name());
        run.setFinishedAt(LocalDateTime.now());
        run.setHitCount(hitCount);
        run.setHitRate(validCases > 0 ? (double) hitCount / validCases : 0);
        run.setMrr(validCases > 0 ? mrrSum / validCases : 0);
        run.setAvgLatencyMs(validCases > 0 ? totalLatency / validCases : 0);
        run.setAvgScore(validCases > 0 ? totalScore / validCases : 0);
        evalRunMapper.updateById(run);
    }

    private boolean checkHit(String expectedSourcesJson, List<RetrievedChunkResponse> chunks) {
        if (expectedSourcesJson == null || expectedSourcesJson.isBlank()) return false;
        String lowerExpected = expectedSourcesJson.toLowerCase();
        for (RetrievedChunkResponse chunk : chunks) {
            if (lowerExpected.contains(String.valueOf(chunk.documentId()))) {
                return true;
            }
        }
        return false;
    }

    private int findRank(String expectedSourcesJson, List<RetrievedChunkResponse> chunks) {
        if (expectedSourcesJson == null || expectedSourcesJson.isBlank()) return 0;
        String lowerExpected = expectedSourcesJson.toLowerCase();
        for (int i = 0; i < chunks.size(); i++) {
            if (lowerExpected.contains(String.valueOf(chunks.get(i).documentId()))) {
                return i + 1;
            }
        }
        return 0;
    }

    private double calculateScore(String expectedAnswer, String actualAnswer) {
        if (expectedAnswer == null || expectedAnswer.isBlank()) return 0;
        if (actualAnswer == null || actualAnswer.isBlank()) return 0;

        Set<String> expectedWords = new HashSet<>(Arrays.asList(expectedAnswer.toLowerCase().split("\\W+")));
        Set<String> actualWords = new HashSet<>(Arrays.asList(actualAnswer.toLowerCase().split("\\W+")));

        if (expectedWords.isEmpty()) return 0;
        Set<String> intersection = new HashSet<>(expectedWords);
        intersection.retainAll(actualWords);
        return (double) intersection.size() / expectedWords.size();
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }

    private EvalRunResponse toResponse(EvalRun run, List<EvalCase> cases) {
        List<EvalCaseResponse> caseResponses = cases.stream()
                .map(c -> new EvalCaseResponse(
                        c.getId(), c.getQuestion(), c.getExpectedAnswer(), c.getExpectedSources(),
                        c.getActualAnswer(), c.getActualSources(), c.getRetrievalHit(),
                        c.getRetrievalRank(), c.getScore(), c.getLatencyMs(), c.getErrorMessage()
                ))
                .toList();

        return new EvalRunResponse(
                run.getId(), run.getKnowledgeBaseId(), run.getName(), run.getStatus(),
                run.getTotalCases(), run.getHitCount(), run.getHitRate(), run.getMrr(),
                run.getAvgLatencyMs(), run.getAvgScore(),
                run.getStartedAt(), run.getFinishedAt(), run.getCreatedAt()
        );
    }
}
