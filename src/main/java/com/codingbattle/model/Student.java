package com.codingbattle.model;

import java.time.LocalDateTime;

/**
 * Student class extending User.
 * Demonstrates Inheritance, Polymorphism, and rich Academic Tracking metrics.
 */
public class Student extends User {
    private String fullName;
    private int totalPoints;
    private int solvedProblems;
    private int currentRank;
    private int battlesParticipated;
    private int currentStreak;
    private int highestStreak;
    private int lowestStreak;
    private double attendanceRate;
    private String riskStatus;
    private String batch;
    private boolean dailyActive;

    public Student() {
        super();
        setRole("STUDENT");
        this.currentStreak = 0;
        this.highestStreak = 0;
        this.lowestStreak = 0;
        this.attendanceRate = 100.0;
        this.riskStatus = "Optimal";
        this.batch = "CSE-2026-A";
        this.dailyActive = true;
    }

    public Student(int userId, String username, String email, String password, LocalDateTime createdAt) {
        super(userId, username, email, password, "STUDENT", createdAt);
        this.fullName = username;
        this.totalPoints = 0;
        this.solvedProblems = 0;
        this.currentRank = 0;
        this.battlesParticipated = 0;
        this.currentStreak = 0;
        this.highestStreak = 0;
        this.lowestStreak = 0;
        this.attendanceRate = 100.0;
        this.riskStatus = "Optimal";
        this.batch = "CSE-2026-A";
        this.dailyActive = true;
    }

    public String getFullName() {
        return fullName != null ? fullName : getUsername();
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getHighestStreak() {
        return highestStreak;
    }

    public void setHighestStreak(int highestStreak) {
        this.highestStreak = highestStreak;
    }

    public int getLowestStreak() {
        return lowestStreak;
    }

    public void setLowestStreak(int lowestStreak) {
        this.lowestStreak = lowestStreak;
    }

    public double getAttendanceRate() {
        return attendanceRate;
    }

    public void setAttendanceRate(double attendanceRate) {
        this.attendanceRate = attendanceRate;
    }

    public String getRiskStatus() {
        return riskStatus;
    }

    public void setRiskStatus(String riskStatus) {
        this.riskStatus = riskStatus;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public boolean isDailyActive() {
        return dailyActive;
    }

    public void setDailyActive(boolean dailyActive) {
        this.dailyActive = dailyActive;
    }

    @Override
    public String getRoleDescription() {
        return "Student Coder: Can practice challenges, participate in battles, earn badges and climb the leaderboard.";
    }
}
