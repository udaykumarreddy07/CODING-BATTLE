package com.codingbattle.dao;

import com.codingbattle.database.DatabaseConnection;
import com.codingbattle.model.Submission;
import com.codingbattle.model.SubmissionStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Submission Data Access Object for recording and querying code submissions.
 */
public class SubmissionDAO {

    private static final Logger LOGGER = Logger.getLogger(SubmissionDAO.class.getName());

    public int saveSubmission(Submission s) {
        String sql = """
            INSERT INTO submissions (user_id, problem_id, battle_id, source_code, status, score, execution_time, submitted_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, s.getUserId());
            ps.setInt(2, s.getProblemId());
            if (s.getBattleId() != null && s.getBattleId() > 0) {
                ps.setInt(3, s.getBattleId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, s.getSourceCode());
            ps.setString(5, s.getStatus().name());
            ps.setInt(6, s.getScore());
            ps.setLong(7, s.getExecutionTimeMs());
            ps.setTimestamp(8, Timestamp.valueOf(s.getSubmittedAt() != null ? s.getSubmittedAt() : LocalDateTime.now()));

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int genId = rs.getInt(1);
                        s.setSubmissionId(genId);
                        return genId;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving submission", e);
        }
        return -1;
    }

    public List<Submission> getSubmissionsByUserId(int userId) {
        List<Submission> list = new ArrayList<>();
        String sql = """
            SELECT s.*, p.title as problem_title, u.username
            FROM submissions s
            JOIN problems p ON s.problem_id = p.problem_id
            JOIN users u ON s.user_id = u.user_id
            WHERE s.user_id = ?
            ORDER BY s.submission_id DESC
        """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToSubmission(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching user submissions", e);
        }
        return list;
    }

    public List<Submission> getAllSubmissions() {
        List<Submission> list = new ArrayList<>();
        String sql = """
            SELECT s.*, p.title as problem_title, u.username
            FROM submissions s
            JOIN problems p ON s.problem_id = p.problem_id
            JOIN users u ON s.user_id = u.user_id
            ORDER BY s.submission_id DESC
        """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToSubmission(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all submissions", e);
        }
        return list;
    }

    public Map<String, Integer> getUserPerformanceStats(int userId) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("TOTAL_ATTEMPTED", 0);
        stats.put("TOTAL_SOLVED", 0);
        stats.put("EASY_SOLVED", 0);
        stats.put("MEDIUM_SOLVED", 0);
        stats.put("HARD_SOLVED", 0);

        String sql = """
            SELECT p.difficulty, COUNT(DISTINCT s.problem_id) as count
            FROM submissions s
            JOIN problems p ON s.problem_id = p.problem_id
            WHERE s.user_id = ? AND s.status = 'ACCEPTED'
            GROUP BY p.difficulty
        """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                int totalSolved = 0;
                while (rs.next()) {
                    String diff = rs.getString("difficulty");
                    int count = rs.getInt("count");
                    totalSolved += count;
                    if ("EASY".equalsIgnoreCase(diff)) stats.put("EASY_SOLVED", count);
                    else if ("MEDIUM".equalsIgnoreCase(diff)) stats.put("MEDIUM_SOLVED", count);
                    else if ("HARD".equalsIgnoreCase(diff)) stats.put("HARD_SOLVED", count);
                }
                stats.put("TOTAL_SOLVED", totalSolved);
            }

            // Total attempted distinct problems
            String attSql = "SELECT COUNT(DISTINCT problem_id) FROM submissions WHERE user_id = ?";
            try (PreparedStatement psAtt = conn.prepareStatement(attSql)) {
                psAtt.setInt(1, userId);
                try (ResultSet rsAtt = psAtt.executeQuery()) {
                    if (rsAtt.next()) {
                        stats.put("TOTAL_ATTEMPTED", rsAtt.getInt(1));
                    }
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching user performance stats", e);
        }
        return stats;
    }

    private Submission mapRowToSubmission(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("submitted_at");
        int battleIdVal = rs.getInt("battle_id");
        Integer battleId = rs.wasNull() ? null : battleIdVal;

        Submission s = new Submission(
                rs.getInt("submission_id"),
                rs.getInt("user_id"),
                rs.getInt("problem_id"),
                battleId,
                rs.getString("source_code"),
                SubmissionStatus.fromString(rs.getString("status")),
                rs.getInt("score"),
                rs.getLong("execution_time"),
                ts != null ? ts.toLocalDateTime() : LocalDateTime.now()
        );
        s.setProblemTitle(rs.getString("problem_title"));
        s.setUsername(rs.getString("username"));
        return s;
    }
}
