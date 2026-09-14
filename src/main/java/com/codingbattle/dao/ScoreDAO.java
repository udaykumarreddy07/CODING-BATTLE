package com.codingbattle.dao;

import com.codingbattle.database.DatabaseConnection;
import com.codingbattle.model.LeaderboardEntry;
import com.codingbattle.model.Score;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Score Data Access Object handling contest scores, ranking, and leaderboard calculations.
 */
public class ScoreDAO {

    private static final Logger LOGGER = Logger.getLogger(ScoreDAO.class.getName());

    public List<Score> getScoresByBattleId(int battleId) {
        List<Score> list = new ArrayList<>();
        String sql = """
            SELECT sc.*, u.username
            FROM scores sc
            JOIN users u ON sc.user_id = u.user_id
            WHERE sc.battle_id = ?
            ORDER BY sc.total_score DESC, sc.problems_solved DESC
        """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, battleId);
            try (ResultSet rs = ps.executeQuery()) {
                int currentRank = 1;
                while (rs.next()) {
                    Score s = new Score(
                            rs.getInt("score_id"),
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getInt("battle_id"),
                            rs.getInt("total_score"),
                            currentRank++,
                            rs.getInt("problems_solved")
                    );
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching battle scores", e);
        }
        return list;
    }

    public void upsertBattleScore(int userId, int battleId, int scoreEarned) {
        String selectSql = "SELECT score_id, total_score, problems_solved FROM scores WHERE user_id = ? AND battle_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(selectSql)) {

            ps.setInt(1, userId);
            ps.setInt(2, battleId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int scoreId = rs.getInt("score_id");
                    int newTotal = rs.getInt("total_score") + scoreEarned;
                    int newSolved = rs.getInt("problems_solved") + 1;

                    String updateSql = "UPDATE scores SET total_score = ?, problems_solved = ? WHERE score_id = ?";
                    try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                        updatePs.setInt(1, newTotal);
                        updatePs.setInt(2, newSolved);
                        updatePs.setInt(3, scoreId);
                        updatePs.executeUpdate();
                    }
                } else {
                    String insertSql = "INSERT INTO scores (user_id, battle_id, total_score, rank_position, problems_solved) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        insertPs.setInt(1, userId);
                        insertPs.setInt(2, battleId);
                        insertPs.setInt(3, scoreEarned);
                        insertPs.setInt(4, 1);
                        insertPs.setInt(5, 1);
                        insertPs.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error upserting battle score", e);
        }
    }

    public List<LeaderboardEntry> getGlobalLeaderboardData() {
        List<LeaderboardEntry> list = new ArrayList<>();
        String sql = """
            SELECT u.user_id, u.username,
                   COALESCE(SUM(CASE WHEN s.status = 'ACCEPTED' THEN s.score ELSE 0 END), 0) as total_points,
                   COUNT(DISTINCT CASE WHEN s.status = 'ACCEPTED' THEN s.problem_id END) as solved_count,
                   COUNT(s.submission_id) as total_submissions,
                   COUNT(CASE WHEN s.status = 'ACCEPTED' THEN 1 END) as accepted_submissions
            FROM users u
            LEFT JOIN submissions s ON u.user_id = s.user_id
            WHERE u.role = 'STUDENT'
            GROUP BY u.user_id, u.username
            ORDER BY total_points DESC, solved_count DESC
        """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int rank = 1;
            while (rs.next()) {
                int totalSubs = rs.getInt("total_submissions");
                int acceptedSubs = rs.getInt("accepted_submissions");
                double successRate = (totalSubs > 0) ? ((double) acceptedSubs / totalSubs) * 100.0 : 0.0;

                LeaderboardEntry entry = new LeaderboardEntry(
                        rank++,
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getInt("total_points"),
                        rs.getInt("solved_count"),
                        0, // Battles won computed separately if needed
                        Math.round(successRate * 10.0) / 10.0
                );
                list.add(entry);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching global leaderboard data", e);
        }
        return list;
    }
}
