package com.example.copilot.exception;

import com.example.copilot.common.ErrorCode;

public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(ErrorCode.FORBIDDEN, message);
    }
}
