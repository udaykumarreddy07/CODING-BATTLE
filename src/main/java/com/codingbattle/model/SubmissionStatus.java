package com.codingbattle.model;

/**
 * Result status for code submissions.
 */
public enum SubmissionStatus {
    ACCEPTED("Accepted", "#2ea043"),
    WRONG_ANSWER("Wrong Answer", "#f85149"),
    TIME_LIMIT_EXCEEDED("Time Limit Exceeded", "#d29922"),
    COMPILATION_ERROR("Compilation Error", "#f85149"),
    RUNTIME_ERROR("Runtime Error", "#f85149"),
    SECURITY_VIOLATION("Security Violation", "#f85149"),
    PENDING("Evaluating...", "#58a6ff");

    private final String displayName;
    private final String colorHex;

    SubmissionStatus(String displayName, String colorHex) {
        this.displayName = displayName;
        this.colorHex = colorHex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorHex() {
        return colorHex;
    }

    public static SubmissionStatus fromString(String value) {
        if (value == null) return PENDING;
        String clean = value.trim().replace(" ", "_").toUpperCase();
        try {
            return SubmissionStatus.valueOf(clean);
        } catch (IllegalArgumentException e) {
            return PENDING;
        }
    }
}
