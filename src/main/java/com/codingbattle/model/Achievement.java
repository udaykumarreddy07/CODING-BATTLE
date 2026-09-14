package com.codingbattle.model;

import java.time.LocalDateTime;

/**
 * Gamification Achievement / Badge model.
 */
public class Achievement {
    private int achievementId;
    private String badgeCode;
    private String name;
    private String description;
    private String icon;
    private int pointsReward;
    private boolean unlocked;
    private LocalDateTime earnedAt;

    public Achievement() {}

    public Achievement(int achievementId, String badgeCode, String name, String description,
                       String icon, int pointsReward) {
        this.achievementId = achievementId;
        this.badgeCode = badgeCode;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.pointsReward = pointsReward;
        this.unlocked = false;
    }

    public int getAchievementId() {
        return achievementId;
    }

    public void setAchievementId(int achievementId) {
        this.achievementId = achievementId;
    }

    public String getBadgeCode() {
        return badgeCode;
    }

    public void setBadgeCode(String badgeCode) {
        this.badgeCode = badgeCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getPointsReward() {
        return pointsReward;
    }

    public void setPointsReward(int pointsReward) {
        this.pointsReward = pointsReward;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public LocalDateTime getEarnedAt() {
        return earnedAt;
    }

    public void setEarnedAt(LocalDateTime earnedAt) {
        this.earnedAt = earnedAt;
    }
}
