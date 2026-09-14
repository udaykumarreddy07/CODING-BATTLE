package com.codingbattle.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Battle model representing a timed programming contest.
 * Demonstrates Composition: Battle contains a collection of Problems and participant data.
 */
public class Battle {
    private int battleId;
    private String battleName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int durationMinutes;
    private BattleStatus status;
    private int maxParticipants;
    private List<Problem> problems;
    private int totalParticipants;

    public Battle() {
        this.status = BattleStatus.UPCOMING;
        this.durationMinutes = 30;
        this.maxParticipants = 100;
        this.problems = new ArrayList<>();
    }

    public Battle(int battleId, String battleName, LocalDateTime startTime, LocalDateTime endTime,
                  int durationMinutes, BattleStatus status, int maxParticipants) {
        this.battleId = battleId;
        this.battleName = battleName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes > 0 ? durationMinutes : 30;
        this.status = status != null ? status : BattleStatus.UPCOMING;
        this.maxParticipants = maxParticipants > 0 ? maxParticipants : 100;
        this.problems = new ArrayList<>();
    }

    public int getBattleId() {
        return battleId;
    }

    public void setBattleId(int battleId) {
        this.battleId = battleId;
    }

    public String getBattleName() {
        return battleName;
    }

    public void setBattleName(String battleName) {
        this.battleName = battleName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public BattleStatus getStatus() {
        return status;
    }

    public void setStatus(BattleStatus status) {
        this.status = status;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public List<Problem> getProblems() {
        return problems;
    }

    public void setProblems(List<Problem> problems) {
        this.problems = problems != null ? problems : new ArrayList<>();
    }

    public void addProblem(Problem problem) {
        this.problems.add(problem);
    }

    public int getTotalParticipants() {
        return totalParticipants;
    }

    public void setTotalParticipants(int totalParticipants) {
        this.totalParticipants = totalParticipants;
    }

    @Override
    public String toString() {
        return battleName + " (" + status + " - " + durationMinutes + " mins)";
    }
}
