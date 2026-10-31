package com.codingbattle.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing an Academic Examination or Homework Assignment.
 * Faculty can schedule exams, monitor live proctoring, and review submissions.
 */
public class Assignment {
    private int id;
    private String title;
    private String description;
    private String category;
    private LocalDateTime deadline;
    private int durationMinutes;
    private int totalMarks;
    private int problemCount;
    private List<Problem> problems;
    private boolean proctoringEnabled;
    private int submissionsCount;
    private int totalAssigned;
    private String status;

    public Assignment() {
        this.problems = new ArrayList<>();
        this.proctoringEnabled = true;
        this.status = "ACTIVE";
    }

    public Assignment(int id, String title, String description, String category, LocalDateTime deadline,
                      int durationMinutes, int totalMarks, boolean proctoringEnabled, int totalAssigned) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.deadline = deadline;
        this.durationMinutes = durationMinutes;
        this.totalMarks = totalMarks;
        this.proctoringEnabled = proctoringEnabled;
        this.totalAssigned = totalAssigned;
        this.submissionsCount = 0;
        this.status = "ACTIVE";
        this.problems = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
    }

    public int getProblemCount() {
        return problems != null ? problems.size() : problemCount;
    }

    public void setProblemCount(int problemCount) {
        this.problemCount = problemCount;
    }

    public List<Problem> getProblems() {
        return problems;
    }

    public void setProblems(List<Problem> problems) {
        this.problems = problems;
        if (problems != null) {
            this.problemCount = problems.size();
        }
    }

    public boolean isProctoringEnabled() {
        return proctoringEnabled;
    }

    public void setProctoringEnabled(boolean proctoringEnabled) {
        this.proctoringEnabled = proctoringEnabled;
    }

    public int getSubmissionsCount() {
        return submissionsCount;
    }

    public void setSubmissionsCount(int submissionsCount) {
        this.submissionsCount = submissionsCount;
    }

    public int getTotalAssigned() {
        return totalAssigned;
    }

    public void setTotalAssigned(int totalAssigned) {
        this.totalAssigned = totalAssigned;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return title + " (" + totalMarks + " pts, " + durationMinutes + " mins)";
    }
}
