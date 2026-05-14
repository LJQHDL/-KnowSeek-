package com.example.copilot.common;

public final class ErrorCode {

    public static final int VALIDATION_ERROR = 40001;
    public static final int AUTH_REQUIRED = 40101;
    public static final int TOKEN_EXPIRED = 40102;
    public static final int FORBIDDEN = 40301;
    public static final int NOT_FOUND = 40401;
    public static final int CONFLICT = 40901;
    public static final int INTERNAL_ERROR = 50001;

    private ErrorCode() {
    }
}
