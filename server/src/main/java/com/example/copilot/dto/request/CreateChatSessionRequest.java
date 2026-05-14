package com.example.copilot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateChatSessionRequest(
        @NotNull(message = "知识库ID不能为空")
        Long knowledgeBaseId,

        @NotBlank(message = "会话标题不能为空")
        @Size(max = 255, message = "会话标题长度不能超过255")
        String title
) {
}
