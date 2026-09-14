package com.codingbattle.model;

/**
 * Difficulty levels for coding problems.
 */
public enum Difficulty {
    EASY(100, "#2ea043"),
    MEDIUM(200, "#d29922"),
    HARD(300, "#f85149");

    private final int defaultPoints;
    private final String colorHex;

    Difficulty(int defaultPoints, String colorHex) {
        this.defaultPoints = defaultPoints;
        this.colorHex = colorHex;
    }

    public int getDefaultPoints() {
        return defaultPoints;
    }

    public String getColorHex() {
        return colorHex;
    }

    public static Difficulty fromString(String value) {
        if (value == null) return EASY;
        try {
            return Difficulty.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return EASY;
        }
    }
}
