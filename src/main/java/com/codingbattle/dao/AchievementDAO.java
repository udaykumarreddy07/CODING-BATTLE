package com.codingbattle.dao;

import com.codingbattle.database.DatabaseConnection;
import com.codingbattle.model.Achievement;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Achievement Data Access Object for managing gamification badges and student progress.
 */
public class AchievementDAO {

    private static final Logger LOGGER = Logger.getLogger(AchievementDAO.class.getName());

    public List<Achievement> getAllAchievementsWithUserStatus(int userId) {
        List<Achievement> list = new ArrayList<>();
        String sql = """
            SELECT a.*, ua.earned_at
            FROM achievements a
            LEFT JOIN user_achievements ua ON a.achievement_id = ua.achievement_id AND ua.user_id = ?
            ORDER BY a.achievement_id ASC
        """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Achievement ach = new Achievement(
                            rs.getInt("achievement_id"),
                            rs.getString("badge_code"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getString("icon"),
                            rs.getInt("points_reward")
                    );
                    Timestamp earnedTs = rs.getTimestamp("earned_at");
                    if (earnedTs != null) {
                        ach.setUnlocked(true);
                        ach.setEarnedAt(earnedTs.toLocalDateTime());
                    } else {
                        ach.setUnlocked(false);
                    }
                    list.add(ach);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching user achievements", e);
        }
        return list;
    }

    public boolean unlockAchievement(int userId, String badgeCode) {
        String checkSql = """
            SELECT ua.achievement_id FROM user_achievements ua
            JOIN achievements a ON ua.achievement_id = a.achievement_id
            WHERE ua.user_id = ? AND a.badge_code = ?
        """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {

            checkPs.setInt(1, userId);
            checkPs.setString(2, badgeCode);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    return false; // Already unlocked
                }
            }

            // Insert new unlock
            String insertSql = """
                INSERT INTO user_achievements (user_id, achievement_id, earned_at)
                SELECT ?, achievement_id, ? FROM achievements WHERE badge_code = ?
            """;
            try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                insertPs.setInt(1, userId);
                insertPs.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                insertPs.setString(3, badgeCode);
                return insertPs.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error unlocking achievement: " + badgeCode, e);
            return false;
        }
    }
}
