package com.example.copilot.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RequestTraceFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return LoggingUtils.shouldSkipLogging(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        String traceId = resolveTraceId(request);
        MDC.put(TRACE_ID_KEY, traceId);
        wrappedResponse.setHeader(TRACE_ID_HEADER, traceId);

        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long cost = System.currentTimeMillis() - startTime;
            logRequest(wrappedRequest, traceId);
            logResponse(wrappedRequest, wrappedResponse, traceId, cost);
            wrappedResponse.copyBodyToResponse();
            MDC.remove(TRACE_ID_KEY);
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, String traceId) {
        String body = LoggingUtils.extractVisiblePayload(
                request.getContentAsByteArray(),
                request.getContentType(),
                request.getCharacterEncoding()
        );

        log.info("""
                [REQUEST] {} {}
                traceId={}
                time={}
                ip={}
                userAgent={}
                params={}
                headers={}
                body={}
                """,
                request.getMethod(),
                request.getRequestURI(),
                traceId,
                OffsetDateTime.now(ZoneOffset.ofHours(8)),
                LoggingUtils.getClientIp(request),
                request.getHeader("User-Agent"),
                LoggingUtils.getQueryParams(request, objectMapper),
                LoggingUtils.buildHeaderSummary(request, objectMapper),
                body
        );
    }

    private void logResponse(ContentCachingRequestWrapper request,
                             ContentCachingResponseWrapper response,
                             String traceId,
                             long cost) {
        String responseBody = LoggingUtils.extractVisiblePayload(
                response.getContentAsByteArray(),
                response.getContentType(),
                response.getCharacterEncoding()
        );

        log.info("""
                [RESPONSE] {} {}
                traceId={}
                status={}
                cost={}ms
                response={}
                """,
                request.getMethod(),
                request.getRequestURI(),
                traceId,
                response.getStatus(),
                cost,
                responseBody
        );
    }

    private String resolveTraceId(HttpServletRequest request) {
        String incomingTraceId = request.getHeader(TRACE_ID_HEADER);
        if (incomingTraceId != null && !incomingTraceId.isBlank()) {
            return incomingTraceId;
        }
        return UUID.randomUUID().toString().replace("-", "");
    }
}
