package com.example.copilot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("eval_cases")
public class EvalCase {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long evalRunId;
    private String question;
    private String expectedAnswer;
    private String expectedSources;
    private String actualAnswer;
    private String actualSources;
    private Boolean retrievalHit;
    private Integer retrievalRank;
    private Double score;
    private Long latencyMs;
    private String errorMessage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEvalRunId() {
        return evalRunId;
    }

    public void setEvalRunId(Long evalRunId) {
        this.evalRunId = evalRunId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getExpectedAnswer() {
        return expectedAnswer;
    }

    public void setExpectedAnswer(String expectedAnswer) {
        this.expectedAnswer = expectedAnswer;
    }

    public String getExpectedSources() {
        return expectedSources;
    }

    public void setExpectedSources(String expectedSources) {
        this.expectedSources = expectedSources;
    }

    public String getActualAnswer() {
        return actualAnswer;
    }

    public void setActualAnswer(String actualAnswer) {
        this.actualAnswer = actualAnswer;
    }

    public String getActualSources() {
        return actualSources;
    }

    public void setActualSources(String actualSources) {
        this.actualSources = actualSources;
    }

    public Boolean getRetrievalHit() {
        return retrievalHit;
    }

    public void setRetrievalHit(Boolean retrievalHit) {
        this.retrievalHit = retrievalHit;
    }

    public Integer getRetrievalRank() {
        return retrievalRank;
    }

    public void setRetrievalRank(Integer retrievalRank) {
        this.retrievalRank = retrievalRank;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
