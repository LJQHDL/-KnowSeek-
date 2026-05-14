package com.example.copilot.exception;

import com.example.copilot.common.ApiResponse;
import com.example.copilot.common.ErrorCode;
import com.example.copilot.logging.LoggingUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("[BUSINESS-EXCEPTION] path={} method={} params={} message={}",
                request.getRequestURI(),
                request.getMethod(),
                LoggingUtils.getQueryParams(request, objectMapper),
                ex.getMessage(),
                ex);
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.VALIDATION_ERROR, message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
        log.warn("[BUSINESS-EXCEPTION] path={} method={} params={} message={}",
                request.getRequestURI(),
                request.getMethod(),
                LoggingUtils.getQueryParams(request, objectMapper),
                ex.getMessage(),
                ex);
        return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.VALIDATION_ERROR, ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("[BUSINESS-EXCEPTION] path={} method={} params={} message={}",
                request.getRequestURI(),
                request.getMethod(),
                LoggingUtils.getQueryParams(request, objectMapper),
                ex.getMessage(),
                ex);
        String message = "请求体缺失或 JSON 格式不正确";
        return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.VALIDATION_ERROR, message));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        log.warn("[BUSINESS-EXCEPTION] path={} method={} params={} message={}",
                request.getRequestURI(),
                request.getMethod(),
                LoggingUtils.getQueryParams(request, objectMapper),
                ex.getMessage(),
                ex);
        String message = "上传文件过大，超过服务端限制，请压缩后重试";
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR, message));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex, HttpServletRequest request) {
        log.warn("[BUSINESS-EXCEPTION] path={} method={} params={} code={} message={}",
                request.getRequestURI(),
                request.getMethod(),
                LoggingUtils.getQueryParams(request, objectMapper),
                ex.getCode(),
                ex.getMessage(),
                ex);
        HttpStatus status = ex instanceof ForbiddenException ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;
        if (ex instanceof NotFoundException) {
            status = HttpStatus.NOT_FOUND;
        }
        return ResponseEntity.status(status).body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception ex, HttpServletRequest request) {
        log.error("[SYSTEM-EXCEPTION] path={} method={} params={} message={}",
                request.getRequestURI(),
                request.getMethod(),
                LoggingUtils.getQueryParams(request, objectMapper),
                ex.getMessage(),
                ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR, ex.getMessage()));
    }
}
