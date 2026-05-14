package com.example.copilot.controller;

import com.example.copilot.common.ApiResponse;
import com.example.copilot.dto.request.CreateFeedbackRequest;
import com.example.copilot.entity.AnswerFeedback;
import com.example.copilot.security.UserPrincipal;
import com.example.copilot.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/{id}/feedback")
    public ApiResponse<AnswerFeedback> submitFeedback(@AuthenticationPrincipal UserPrincipal principal,
                                                      @PathVariable Long id,
                                                      @Valid @RequestBody CreateFeedbackRequest request) {
        return ApiResponse.success(feedbackService.submitFeedback(id, principal.getId(), request.rating(), request.comment()));
    }
}
