package com.codingbattle.model;

/**
 * Encapsulates a test case for solution verification.
 */
public class TestCase {
    private int testId;
    private int problemId;
    private String inputData;
    private String expectedOutput;
    private boolean sample;

    public TestCase() {}

    public TestCase(int testId, int problemId, String inputData, String expectedOutput, boolean sample) {
        this.testId = testId;
        this.problemId = problemId;
        this.inputData = inputData != null ? inputData : "";
        this.expectedOutput = expectedOutput != null ? expectedOutput.trim() : "";
        this.sample = sample;
    }

    public TestCase(int problemId, String inputData, String expectedOutput, boolean sample) {
        this(0, problemId, inputData, expectedOutput, sample);
    }

    public int getTestId() {
        return testId;
    }

    public void setTestId(int testId) {
        this.testId = testId;
    }

    public int getProblemId() {
        return problemId;
    }

    public void setProblemId(int problemId) {
        this.problemId = problemId;
    }

    public String getInputData() {
        return inputData;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public boolean isSample() {
        return sample;
    }

    public void setSample(boolean sample) {
        this.sample = sample;
    }

    @Override
    public String toString() {
        return "TestCase{" +
                "testId=" + testId +
                ", problemId=" + problemId +
                ", isSample=" + sample +
                '}';
    }
}
