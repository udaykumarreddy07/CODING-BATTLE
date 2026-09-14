package com.codingbattle.engine;

import com.codingbattle.model.Difficulty;

/**
 * Abstraction Interface for Scoring Logic.
 * Demonstrates Abstraction and Strategy Pattern in OOP.
 */
public interface ScoreCalculator {
    /**
     * Calculates the score based on difficulty, test cases passed, and execution time.
     *
     * @param difficulty       Problem difficulty (EASY, MEDIUM, HARD)
     * @param basePoints       Configured points for the problem
     * @param passedTestCases  Number of test cases passed
     * @param totalTestCases   Total number of test cases evaluated
     * @param executionTimeMs  Total execution time in milliseconds
     * @return Final awarded score
     */
    int calculateScore(Difficulty difficulty, int basePoints, int passedTestCases, int totalTestCases, long executionTimeMs);
}
