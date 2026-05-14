package com.example.copilot.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

@UtilityClass
public class LoggingUtils {

    private static final int MAX_PAYLOAD_LENGTH = 4000;
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final String MASKED_TEXT = "***";
    private static final String[] SENSITIVE_KEYS = {"password", "token", "secret", "authorization", "apiKey", "api-key"};

    public static String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp;
        }
        return request.getRemoteAddr();
    }

    public static String getQueryParams(HttpServletRequest request, ObjectMapper objectMapper) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.isEmpty()) {
            return "{}";
        }

        Map<String, Object> sanitized = new LinkedHashMap<>();
        parameterMap.forEach((key, value) -> sanitized.put(key, isSensitiveKey(key) ? MASKED_TEXT : value));
        return toJson(sanitized, objectMapper);
    }

    public static String buildHeaderSummary(HttpServletRequest request, ObjectMapper objectMapper) {
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(headerName)) {
                headers.put(headerName, MASKED_TEXT);
            } else {
                headers.put(headerName, request.getHeader(headerName));
            }
        }
        return toJson(headers, objectMapper);
    }

    public static String extractVisiblePayload(byte[] content, String contentType, String characterEncoding) {
        if (content == null || content.length == 0 || !isVisibleContentType(contentType)) {
            return "";
        }

        Charset charset = resolveCharset(contentType, characterEncoding);
        String payload = new String(content, charset);
        return abbreviate(maskSensitiveText(payload));
    }

    public static String toJson(Object value, ObjectMapper objectMapper) {
        try {
            return abbreviate(objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException ex) {
            return abbreviate(String.valueOf(value));
        }
    }

    public static String sanitizeObject(Object value, ObjectMapper objectMapper) {
        if (value == null) {
            return "null";
        }
        if (value instanceof HttpServletRequest) {
            return "HttpServletRequest";
        }
        return toJson(value, objectMapper);
    }

    public static boolean shouldSkipLogging(String path) {
        return PATH_MATCHER.match("/actuator/**", path)
                || PATH_MATCHER.match("/v3/api-docs/**", path)
                || PATH_MATCHER.match("/swagger-ui/**", path)
                || "/doc.html".equals(path);
    }

    private static boolean isVisibleContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return true;
        }

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(contentType);
        } catch (Exception ex) {
            return false;
        }

        return MediaType.APPLICATION_JSON.includes(mediaType)
                || MediaType.APPLICATION_XML.includes(mediaType)
                || MediaType.APPLICATION_FORM_URLENCODED.includes(mediaType)
                || MediaType.TEXT_PLAIN.includes(mediaType)
                || MediaType.TEXT_HTML.includes(mediaType)
                || MediaType.TEXT_XML.includes(mediaType)
                || mediaType.getSubtype().toLowerCase().contains("json")
                || mediaType.getSubtype().toLowerCase().contains("xml")
                || mediaType.getSubtype().toLowerCase().contains("text");
    }

    private static Charset resolveCharset(String contentType, String characterEncoding) {
        if (StringUtils.hasText(characterEncoding) && !"ISO-8859-1".equalsIgnoreCase(characterEncoding)) {
            return Charset.forName(characterEncoding);
        }

        if (!StringUtils.hasText(contentType)) {
            return StandardCharsets.UTF_8;
        }

        try {
            MediaType mediaType = MediaType.parseMediaType(contentType);
            if (MediaType.APPLICATION_JSON.includes(mediaType)
                    || MediaType.APPLICATION_XML.includes(mediaType)
                    || MediaType.TEXT_PLAIN.includes(mediaType)
                    || MediaType.TEXT_HTML.includes(mediaType)
                    || MediaType.TEXT_XML.includes(mediaType)
                    || mediaType.getSubtype().toLowerCase().contains("json")
                    || mediaType.getSubtype().toLowerCase().contains("xml")
                    || mediaType.getSubtype().toLowerCase().contains("text")) {
                return StandardCharsets.UTF_8;
            }
        } catch (Exception ignored) {
            return StandardCharsets.UTF_8;
        }

        return StringUtils.hasText(characterEncoding)
                ? Charset.forName(characterEncoding)
                : StandardCharsets.UTF_8;
    }

    private static boolean isSensitiveKey(String key) {
        for (String sensitiveKey : SENSITIVE_KEYS) {
            if (sensitiveKey.equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    private static String maskSensitiveText(String text) {
        String masked = text;
        for (String sensitiveKey : SENSITIVE_KEYS) {
            masked = masked.replaceAll("(?i)(\"" + sensitiveKey + "\"\\s*:\\s*\")([^\"]*)(\")", "$1" + MASKED_TEXT + "$3");
        }
        return masked;
    }

    private static String abbreviate(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        if (value.length() <= MAX_PAYLOAD_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_PAYLOAD_LENGTH) + "...(truncated)";
    }
}
