package com.example.copilot.controller;

import com.example.copilot.common.ApiResponse;
import com.example.copilot.dto.response.LogMessageResponse;
import com.example.copilot.dto.response.LogRetrievalResponse;
import com.example.copilot.service.LogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
@PreAuthorize("hasRole('ADMIN')")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/messages")
    public ApiResponse<Map<String, Object>> queryMessages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Long knowledgeBaseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<LogMessageResponse> items = logService.queryMessages(page, pageSize, knowledgeBaseId, startDate, endDate);
        long total = logService.countMessages(knowledgeBaseId, startDate, endDate);
        return ApiResponse.success(Map.of("items", items, "total", total, "page", page, "pageSize", pageSize));
    }

    @GetMapping("/retrievals")
    public ApiResponse<Map<String, Object>> queryRetrievals(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        List<LogRetrievalResponse> items = logService.queryRetrievals(page, pageSize);
        long total = logService.countRetrievals();
        return ApiResponse.success(Map.of("items", items, "total", total, "page", page, "pageSize", pageSize));
    }
}
