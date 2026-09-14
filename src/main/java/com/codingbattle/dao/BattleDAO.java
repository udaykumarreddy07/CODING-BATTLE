package com.codingbattle.dao;

import com.codingbattle.database.DatabaseConnection;
import com.codingbattle.model.Battle;
import com.codingbattle.model.BattleStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Battle Data Access Object for contest management and problem mapping.
 */
public class BattleDAO {

    private static final Logger LOGGER = Logger.getLogger(BattleDAO.class.getName());
    private final ProblemDAO problemDAO = new ProblemDAO();

    public List<Battle> getAllBattles() {
        List<Battle> battles = new ArrayList<>();
        String sql = "SELECT * FROM battles ORDER BY battle_id DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Battle b = mapRowToBattle(rs);
                b.setProblems(problemDAO.getProblemsByBattleId(b.getBattleId()));
                battles.add(b);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all battles", e);
        }
        return battles;
    }

    public Battle getBattleById(int battleId) {
        String sql = "SELECT * FROM battles WHERE battle_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, battleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Battle b = mapRowToBattle(rs);
                    b.setProblems(problemDAO.getProblemsByBattleId(battleId));
                    return b;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching battle " + battleId, e);
        }
        return null;
    }

    public int addBattle(Battle battle, List<Integer> problemIds) {
        String sql = """
            INSERT INTO battles (battle_name, start_time, end_time, duration, status, max_participants)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, battle.getBattleName());
            ps.setTimestamp(2, battle.getStartTime() != null ? Timestamp.valueOf(battle.getStartTime()) : Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(3, battle.getEndTime() != null ? Timestamp.valueOf(battle.getEndTime()) : Timestamp.valueOf(LocalDateTime.now().plusMinutes(battle.getDurationMinutes())));
            ps.setInt(4, battle.getDurationMinutes());
            ps.setString(5, battle.getStatus().name());
            ps.setInt(6, battle.getMaxParticipants());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int battleId = rs.getInt(1);
                        battle.setBattleId(battleId);
                        if (problemIds != null && !problemIds.isEmpty()) {
                            linkBattleProblems(conn, battleId, problemIds);
                        }
                        return battleId;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding battle", e);
        }
        return -1;
    }

    public boolean updateBattleStatus(int battleId, BattleStatus status) {
        String sql = "UPDATE battles SET status = ? WHERE battle_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, battleId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating battle status", e);
            return false;
        }
    }

    public boolean deleteBattle(int battleId) {
        String sql = "DELETE FROM battles WHERE battle_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, battleId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting battle", e);
            return false;
        }
    }

    private void linkBattleProblems(Connection conn, int battleId, List<Integer> problemIds) throws SQLException {
        String sql = "INSERT INTO battle_problems (battle_id, problem_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Integer pId : problemIds) {
                ps.setInt(1, battleId);
                ps.setInt(2, pId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private Battle mapRowToBattle(ResultSet rs) throws SQLException {
        Timestamp start = rs.getTimestamp("start_time");
        Timestamp end = rs.getTimestamp("end_time");
        return new Battle(
                rs.getInt("battle_id"),
                rs.getString("battle_name"),
                start != null ? start.toLocalDateTime() : null,
                end != null ? end.toLocalDateTime() : null,
                rs.getInt("duration"),
                BattleStatus.fromString(rs.getString("status")),
                rs.getInt("max_participants")
        );
    }
}
