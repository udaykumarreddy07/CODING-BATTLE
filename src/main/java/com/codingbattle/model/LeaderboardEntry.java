package com.codingbattle.model;

/**
 * Model representing a ranked entry in the Leaderboard.
 */
public class LeaderboardEntry implements Comparable<LeaderboardEntry> {
    private int rank;
    private int userId;
    private String username;
    private int totalPoints;
    private int problemsSolved;
    private int battlesWon;
    private double successRate;

    public LeaderboardEntry() {}

    public LeaderboardEntry(int rank, int userId, String username, int totalPoints,
                            int problemsSolved, int battlesWon, double successRate) {
        this.rank = rank;
        this.userId = userId;
        this.username = username;
        this.totalPoints = totalPoints;
        this.problemsSolved = problemsSolved;
        this.battlesWon = battlesWon;
        this.successRate = successRate;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
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

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getProblemsSolved() {
        return problemsSolved;
    }

    public void setProblemsSolved(int problemsSolved) {
        this.problemsSolved = problemsSolved;
    }

    public int getBattlesWon() {
        return battlesWon;
    }

    public void setBattlesWon(int battlesWon) {
        this.battlesWon = battlesWon;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    /**
     * Primary comparison: Points descending, then problems solved descending, then username ascending.
     */
    @Override
    public int compareTo(LeaderboardEntry other) {
        if (this.totalPoints != other.totalPoints) {
            return Integer.compare(other.totalPoints, this.totalPoints);
        }
        if (this.problemsSolved != other.problemsSolved) {
            return Integer.compare(other.problemsSolved, this.problemsSolved);
        }
        return this.username.compareToIgnoreCase(other.username);
    }
}
