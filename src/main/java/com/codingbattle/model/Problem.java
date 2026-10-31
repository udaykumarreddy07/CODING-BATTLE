package com.codingbattle.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Problem model representing a coding challenge.
 * Demonstrates Encapsulation and Composition (contains List of TestCase objects).
 */
public class Problem {
    private int problemId;
    private String title;
    private String description;
    private String inputFormat;
    private String outputFormat;
    private String constraints;
    private Difficulty difficulty;
    private Category category;
    private int points;
    private int timeLimit; // in milliseconds
    private String sampleInput;
    private String sampleOutput;
    private List<TestCase> testCases;
    private boolean solvedByCurrentUser;

    public Problem() {
        this.difficulty = Difficulty.EASY;
        this.category = Category.ARRAYS;
        this.points = 100;
        this.timeLimit = 2000;
        this.testCases = new ArrayList<>();
    }

    public Problem(int problemId, String title, String description, Category category, Difficulty difficulty, int points) {
        this(problemId, title, description, "", "", "", difficulty != null ? difficulty : Difficulty.EASY,
                category != null ? category : Category.ARRAYS, points > 0 ? points : 100, 2000, "", "");
    }

    public Problem(int problemId, String title, String description, String inputFormat,
                   String outputFormat, String constraints, Difficulty difficulty,
                   Category category, int points, int timeLimit,
                   String sampleInput, String sampleOutput) {
        this.problemId = problemId;
        this.title = title;
        this.description = description;
        this.inputFormat = inputFormat;
        this.outputFormat = outputFormat;
        this.constraints = constraints;
        this.difficulty = difficulty != null ? difficulty : Difficulty.EASY;
        this.category = category != null ? category : Category.ARRAYS;
        this.points = points > 0 ? points : this.difficulty.getDefaultPoints();
        this.timeLimit = timeLimit > 0 ? timeLimit : 2000;
        this.sampleInput = sampleInput != null ? sampleInput : "";
        this.sampleOutput = sampleOutput != null ? sampleOutput : "";
        this.testCases = new ArrayList<>();
    }

    public int getProblemId() {
        return problemId;
    }

    public void setProblemId(int problemId) {
        this.problemId = problemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInputFormat() {
        return inputFormat;
    }

    public void setInputFormat(String inputFormat) {
        this.inputFormat = inputFormat;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getConstraints() {
        return constraints;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public String getSampleInput() {
        return sampleInput;
    }

    public void setSampleInput(String sampleInput) {
        this.sampleInput = sampleInput;
    }

    public String getSampleOutput() {
        return sampleOutput;
    }

    public void setSampleOutput(String sampleOutput) {
        this.sampleOutput = sampleOutput;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases != null ? testCases : new ArrayList<>();
    }

    public void addTestCase(TestCase testCase) {
        this.testCases.add(testCase);
    }

    public boolean isSolvedByCurrentUser() {
        return solvedByCurrentUser;
    }

    public void setSolvedByCurrentUser(boolean solvedByCurrentUser) {
        this.solvedByCurrentUser = solvedByCurrentUser;
    }

    @Override
    public String toString() {
        return title + " [" + difficulty + " - " + category.getDisplayName() + "]";
    }
}
