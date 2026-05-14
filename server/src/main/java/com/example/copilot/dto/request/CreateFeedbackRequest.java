package com.example.copilot.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateFeedbackRequest(
        @NotBlank(message = "反馈类型不能为空")
        String rating,

        String comment
) {
}
