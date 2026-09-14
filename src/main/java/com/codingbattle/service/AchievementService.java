package com.codingbattle.service;

import com.codingbattle.dao.AchievementDAO;
import com.codingbattle.dao.SubmissionDAO;
import com.codingbattle.model.Achievement;
import com.codingbattle.model.Submission;
import com.codingbattle.model.SubmissionStatus;

import java.util.List;
import java.util.Map;

/**
 * Service Layer for Gamification, Achievement tracking, and Badge Unlocks.
 */
public class AchievementService {

    private final AchievementDAO achievementDAO;
    private final SubmissionDAO submissionDAO;

    public AchievementService() {
        this.achievementDAO = new AchievementDAO();
        this.submissionDAO = new SubmissionDAO();
    }

    public List<Achievement> getStudentAchievements(int userId) {
        return achievementDAO.getAllAchievementsWithUserStatus(userId);
    }

    /**
     * Checks all badge criteria after a submission and unlocks any newly earned achievements.
     * Returns true if at least one new achievement was unlocked.
     */
    public boolean checkAndAwardBadges(int userId, Submission latestSubmission) {
        boolean anyUnlocked = false;

        Map<String, Integer> stats = submissionDAO.getUserPerformanceStats(userId);
        int totalSolved = stats.getOrDefault("TOTAL_SOLVED", 0);

        // 1. First Solution
        if (totalSolved >= 1) {
            anyUnlocked |= achievementDAO.unlockAchievement(userId, "FIRST_BLOOD");
        }

        // 2. 10 Problems Solved
        if (totalSolved >= 10) {
            anyUnlocked |= achievementDAO.unlockAchievement(userId, "TEN_PROBLEMS");
        }

        // 3. 50 Problems Solved
        if (totalSolved >= 50) {
            anyUnlocked |= achievementDAO.unlockAchievement(userId, "FIFTY_PROBLEMS");
        }

        // 4. Speed Demon (accepted under 250ms)
        if (latestSubmission != null && latestSubmission.getStatus() == SubmissionStatus.ACCEPTED
                && latestSubmission.getExecutionTimeMs() > 0 && latestSubmission.getExecutionTimeMs() <= 250) {
            anyUnlocked |= achievementDAO.unlockAchievement(userId, "SPEED_DEMON");
        }

        // 5. 5-Day Streak
        if (totalSolved >= 5) {
            anyUnlocked |= achievementDAO.unlockAchievement(userId, "STREAK_5");
        }

        return anyUnlocked;
    }

    public void awardBattleWinner(int userId) {
        achievementDAO.unlockAchievement(userId, "BATTLE_WINNER");
    }

    public void awardTopTen(int userId) {
        achievementDAO.unlockAchievement(userId, "TOP_TEN");
    }
}
