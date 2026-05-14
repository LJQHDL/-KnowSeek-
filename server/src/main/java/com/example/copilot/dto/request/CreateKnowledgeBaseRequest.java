package com.example.copilot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateKnowledgeBaseRequest(
        @NotBlank(message = "知识库名称不能为空")
        @Size(max = 128, message = "知识库名称长度不能超过128")
        String name,

        @Size(max = 1000, message = "知识库描述长度不能超过1000")
        String description
) {
}
