package com.example.copilot.service;

import com.example.copilot.entity.AnswerFeedback;

public interface FeedbackService {

    AnswerFeedback submitFeedback(Long messageId, Long userId, String rating, String comment);
}
