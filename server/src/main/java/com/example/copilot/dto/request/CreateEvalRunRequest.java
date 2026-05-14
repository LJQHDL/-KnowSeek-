package com.example.copilot.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateEvalRunRequest(
        @NotBlank(message = "评测名称不能为空")
        String name,

        @NotNull(message = "知识库ID不能为空")
        Long knowledgeBaseId,

        @NotEmpty(message = "评测用例不能为空")
        @Valid
        List<EvalCaseRequest> cases
) {
    public record EvalCaseRequest(
            @NotBlank(message = "问题不能为空")
            String question,

            String expectedAnswer,

            String expectedSources
    ) {
    }
}
