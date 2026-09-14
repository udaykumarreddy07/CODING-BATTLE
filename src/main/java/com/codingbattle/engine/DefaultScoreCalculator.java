package com.codingbattle.engine;

import com.codingbattle.model.Difficulty;

/**
 * Concrete implementation of ScoreCalculator.
 * Implements weighted scoring with partial credit for test cases and a speed bonus.
 */
public class DefaultScoreCalculator implements ScoreCalculator {

    @Override
    public int calculateScore(Difficulty difficulty, int basePoints, int passedTestCases, int totalTestCases, long executionTimeMs) {
        if (totalTestCases <= 0 || passedTestCases <= 0) {
            return 0;
        }

        int points = basePoints > 0 ? basePoints : difficulty.getDefaultPoints();
        double ratio = (double) passedTestCases / totalTestCases;
        int rawScore = (int) Math.round(points * ratio);

        // Speed bonus if all test cases passed and execution is lightning fast (< 250ms)
        if (passedTestCases == totalTestCases && executionTimeMs > 0 && executionTimeMs < 250) {
            int speedBonus = switch (difficulty) {
                case EASY -> 10;
                case MEDIUM -> 25;
                case HARD -> 50;
            };
            rawScore += speedBonus;
        }

        return rawScore;
    }
}
