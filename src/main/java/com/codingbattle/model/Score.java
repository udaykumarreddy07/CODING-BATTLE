package com.codingbattle.model;

/**
 * Encapsulates a participant's score in a specific battle.
 */
public class Score {
    private int scoreId;
    private int userId;
    private String username;
    private int battleId;
    private int totalScore;
    private int rank;
    private int problemsSolved;

    public Score() {}

    public Score(int scoreId, int userId, String username, int battleId, int totalScore, int rank, int problemsSolved) {
        this.scoreId = scoreId;
        this.userId = userId;
        this.username = username;
        this.battleId = battleId;
        this.totalScore = totalScore;
        this.rank = rank;
        this.problemsSolved = problemsSolved;
    }

    public int getScoreId() {
        return scoreId;
    }

    public void setScoreId(int scoreId) {
        this.scoreId = scoreId;
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

    public int getBattleId() {
        return battleId;
    }

    public void setBattleId(int battleId) {
        this.battleId = battleId;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getProblemsSolved() {
        return problemsSolved;
    }

    public void setProblemsSolved(int problemsSolved) {
        this.problemsSolved = problemsSolved;
    }
}
