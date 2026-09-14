package com.codingbattle.model;

import java.time.LocalDateTime;

/**
 * Student class extending User.
 * Demonstrates Inheritance and Polymorphism.
 */
public class Student extends User {
    private int totalPoints;
    private int solvedProblems;
    private int currentRank;
    private int battlesParticipated;

    public Student() {
        super();
        setRole("STUDENT");
    }

    public Student(int userId, String username, String email, String password, LocalDateTime createdAt) {
        super(userId, username, email, password, "STUDENT", createdAt);
        this.totalPoints = 0;
        this.solvedProblems = 0;
        this.currentRank = 0;
        this.battlesParticipated = 0;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getSolvedProblems() {
        return solvedProblems;
    }

    public void setSolvedProblems(int solvedProblems) {
        this.solvedProblems = solvedProblems;
    }

    public int getCurrentRank() {
        return currentRank;
    }

    public void setCurrentRank(int currentRank) {
        this.currentRank = currentRank;
    }

    public int getBattlesParticipated() {
        return battlesParticipated;
    }

    public void setBattlesParticipated(int battlesParticipated) {
        this.battlesParticipated = battlesParticipated;
    }

    @Override
    public String getRoleDescription() {
        return "Student Coder: Can practice challenges, participate in battles, earn badges and climb the leaderboard.";
    }
}
