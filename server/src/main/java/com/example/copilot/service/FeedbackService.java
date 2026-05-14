package com.example.copilot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.copilot.common.ErrorCode;
import com.example.copilot.common.FeedbackRatingEnum;
import com.example.copilot.common.MessageRoleEnum;
import com.example.copilot.entity.AnswerFeedback;
import com.example.copilot.entity.Message;
import com.example.copilot.exception.BusinessException;
import com.example.copilot.exception.NotFoundException;
import com.example.copilot.mapper.AnswerFeedbackMapper;
import com.example.copilot.mapper.MessageMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FeedbackService {

    private final AnswerFeedbackMapper feedbackMapper;
    private final MessageMapper messageMapper;

    public FeedbackService(AnswerFeedbackMapper feedbackMapper, MessageMapper messageMapper) {
        this.feedbackMapper = feedbackMapper;
        this.messageMapper = messageMapper;
    }

    public AnswerFeedback submitFeedback(Long messageId, Long userId, String rating, String comment) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new NotFoundException("消息不存在");
        }
        if (!MessageRoleEnum.ASSISTANT.toDbValue().equals(message.getRole())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "只能对 AI 回答提交反馈");
        }
        if (rating == null || (!FeedbackRatingEnum.UP.toDbValue().equals(rating) && !FeedbackRatingEnum.DOWN.toDbValue().equals(rating))) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "反馈类型无效，必须为 up 或 down");
        }

        AnswerFeedback existing = feedbackMapper.selectOne(new LambdaQueryWrapper<AnswerFeedback>()
                .eq(AnswerFeedback::getMessageId, messageId)
                .eq(AnswerFeedback::getUserId, userId));

        if (existing != null) {
            existing.setRating(rating);
            existing.setComment(comment);
            existing.setCreatedAt(LocalDateTime.now());
            feedbackMapper.updateById(existing);
            return existing;
        }

        AnswerFeedback feedback = new AnswerFeedback();
        feedback.setMessageId(messageId);
        feedback.setUserId(userId);
        feedback.setRating(rating);
        feedback.setComment(comment);
        feedback.setCreatedAt(LocalDateTime.now());
        feedbackMapper.insert(feedback);
        return feedback;
    }
}
