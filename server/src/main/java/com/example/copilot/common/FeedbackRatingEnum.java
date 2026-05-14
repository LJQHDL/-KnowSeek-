package com.example.copilot.common;

public enum FeedbackRatingEnum {
    UP,
    DOWN;

    public String toDbValue() {
        return this.name().toLowerCase();
    }
}
