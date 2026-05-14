package com.example.copilot.common;

public enum MessageRoleEnum {
    USER,
    ASSISTANT;

    public String toDbValue() {
        return this.name().toLowerCase();
    }
}
