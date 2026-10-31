package com.codingbattle.model;

import java.time.LocalDateTime;

/**
 * Model representing a student's submission for an assignment/exam.
 * Evaluators can review code, test case results, adjust scores, and check proctoring flags.
 */
public class AssignmentSubmission {
    private int submissionId;
    private int assignmentId;
    private int studentId;
    private String studentName;
    private String studentEmail;
    private LocalDateTime submittedAt;
    private int score;
    private int maxScore;
    private int testCasesPassed;
    private int totalTestCases;
    private String code;
    private String verdict;
    private String status;
    private int proctoringFlags;

    public AssignmentSubmission() {
        this.submittedAt = LocalDateTime.now();
        this.status = "GRADED";
        this.verdict = "ACCEPTED";
    }

    public AssignmentSubmission(int submissionId, int assignmentId, int studentId, String studentName,
                                String studentEmail, int score, int maxScore, int testCasesPassed,
                                int totalTestCases, String code, String verdict, int proctoringFlags) {
        this.submissionId = submissionId;
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.submittedAt = LocalDateTime.now();
        this.score = score;
        this.maxScore = maxScore;
        this.testCasesPassed = testCasesPassed;
        this.totalTestCases = totalTestCases;
        this.code = code;
        this.verdict = verdict;
        this.status = "GRADED";
        this.proctoringFlags = proctoringFlags;
    }

    public int getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(int submissionId) {
        this.submissionId = submissionId;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public int getTestCasesPassed() {
        return testCasesPassed;
    }

    public void setTestCasesPassed(int testCasesPassed) {
        this.testCasesPassed = testCasesPassed;
    }

    public int getTotalTestCases() {
        return totalTestCases;
    }

    public void setTotalTestCases(int totalTestCases) {
        this.totalTestCases = totalTestCases;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProctoringFlags() {
        return proctoringFlags;
    }

    public void setProctoringFlags(int proctoringFlags) {
        this.proctoringFlags = proctoringFlags;
    }
}
