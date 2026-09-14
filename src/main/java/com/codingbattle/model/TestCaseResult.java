package com.codingbattle.model;

/**
 * Result of evaluating a single test case.
 */
public class TestCaseResult {
    private int testIndex;
    private boolean passed;
    private String input;
    private String expectedOutput;
    private String actualOutput;
    private long executionTimeMs;
    private String errorMessage;

    public TestCaseResult(int testIndex, boolean passed, String input, String expectedOutput,
                          String actualOutput, long executionTimeMs, String errorMessage) {
        this.testIndex = testIndex;
        this.passed = passed;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.actualOutput = actualOutput;
        this.executionTimeMs = executionTimeMs;
        this.errorMessage = errorMessage;
    }

    public int getTestIndex() {
        return testIndex;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getInput() {
        return input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public String getActualOutput() {
        return actualOutput;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "Test Case " + testIndex + ": " + (passed ? "Passed" : "Failed") +
                (errorMessage != null && !errorMessage.isEmpty() ? " (" + errorMessage + ")" : "");
    }
}
