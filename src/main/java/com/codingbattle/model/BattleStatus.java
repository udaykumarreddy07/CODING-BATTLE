package com.codingbattle.model;

/**
 * Status of a coding battle.
 */
public enum BattleStatus {
    UPCOMING("#58a6ff"),
    LIVE("#2ea043"),
    COMPLETED("#8b949e");

    private final String colorHex;

    BattleStatus(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getColorHex() {
        return colorHex;
    }

    public static BattleStatus fromString(String value) {
        if (value == null) return UPCOMING;
        try {
            return BattleStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UPCOMING;
        }
    }
}
