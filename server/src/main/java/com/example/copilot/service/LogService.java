package com.example.copilot.service;

import com.example.copilot.dto.response.LogMessageResponse;
import com.example.copilot.dto.response.LogRetrievalResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface LogService {

    List<LogMessageResponse> queryMessages(int page, int pageSize, Long knowledgeBaseId,
                                           LocalDateTime startDate, LocalDateTime endDate);

    long countMessages(Long knowledgeBaseId, LocalDateTime startDate, LocalDateTime endDate);

    List<LogRetrievalResponse> queryRetrievals(int page, int pageSize);

    long countRetrievals();
}
