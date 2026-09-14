package com.codingbattle.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates a student's code submission.
 */
public class Submission {
    private int submissionId;
    private int userId;
    private String username;
    private int problemId;
    private String problemTitle;
    private Integer battleId; // Can be null if practicing
    private String sourceCode;
    private SubmissionStatus status;
    private int score;
    private long executionTimeMs;
    private LocalDateTime submittedAt;
    private int passedTestCases;
    private int totalTestCases;
    private List<TestCaseResult> testCaseResults;
    private String compilerOutput;

    public Submission() {
        this.status = SubmissionStatus.PENDING;
        this.submittedAt = LocalDateTime.now();
        this.testCaseResults = new ArrayList<>();
    }

    public Submission(int submissionId, int userId, int problemId, Integer battleId,
                      String sourceCode, SubmissionStatus status, int score,
                      long executionTimeMs, LocalDateTime submittedAt) {
        this.submissionId = submissionId;
        this.userId = userId;
        this.problemId = problemId;
        this.battleId = battleId;
        this.sourceCode = sourceCode;
        this.status = status != null ? status : SubmissionStatus.PENDING;
        this.score = score;
        this.executionTimeMs = executionTimeMs;
        this.submittedAt = submittedAt != null ? submittedAt : LocalDateTime.now();
        this.testCaseResults = new ArrayList<>();
    }

    public int getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(int submissionId) {
        this.submissionId = submissionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getProblemId() {
        return problemId;
    }

    public void setProblemId(int problemId) {
        this.problemId = problemId;
    }

    public String getProblemTitle() {
        return problemTitle;
    }

    public void setProblemTitle(String problemTitle) {
        this.problemTitle = problemTitle;
    }

    public Integer getBattleId() {
        return battleId;
    }

    public void setBattleId(Integer battleId) {
        this.battleId = battleId;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public int getPassedTestCases() {
        return passedTestCases;
    }

    public void setPassedTestCases(int passedTestCases) {
        this.passedTestCases = passedTestCases;
    }

    public int getTotalTestCases() {
        return totalTestCases;
    }

    public void setTotalTestCases(int totalTestCases) {
        this.totalTestCases = totalTestCases;
    }

    public List<TestCaseResult> getTestCaseResults() {
        return testCaseResults;
    }

    public void setTestCaseResults(List<TestCaseResult> testCaseResults) {
        this.testCaseResults = testCaseResults != null ? testCaseResults : new ArrayList<>();
    }

    public String getCompilerOutput() {
        return compilerOutput;
    }

    public void setCompilerOutput(String compilerOutput) {
        this.compilerOutput = compilerOutput;
    }
}
