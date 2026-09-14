package com.codingbattle.dao;

import com.codingbattle.database.DatabaseConnection;
import com.codingbattle.model.Category;
import com.codingbattle.model.Difficulty;
import com.codingbattle.model.Problem;
import com.codingbattle.model.TestCase;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Problem Data Access Object for CRUD operations and queries on problems.
 */
public class ProblemDAO {

    private static final Logger LOGGER = Logger.getLogger(ProblemDAO.class.getName());
    private final TestCaseDAO testCaseDAO = new TestCaseDAO();

    public List<Problem> getAllProblems() {
        List<Problem> problems = new ArrayList<>();
        String sql = "SELECT * FROM problems ORDER BY problem_id ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Problem p = mapRowToProblem(rs);
                problems.add(p);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all problems", e);
        }
        return problems;
    }

    public Problem getProblemById(int problemId) {
        String sql = "SELECT * FROM problems WHERE problem_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, problemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Problem p = mapRowToProblem(rs);
                    p.setTestCases(testCaseDAO.getTestCasesByProblemId(problemId));
                    return p;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching problem " + problemId, e);
        }
        return null;
    }

    public int addProblem(Problem problem) {
        String sql = """
            INSERT INTO problems (title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, problem.getTitle());
            ps.setString(2, problem.getDescription());
            ps.setString(3, problem.getInputFormat());
            ps.setString(4, problem.getOutputFormat());
            ps.setString(5, problem.getConstraints());
            ps.setString(6, problem.getDifficulty().name());
            ps.setString(7, problem.getCategory().name());
            ps.setInt(8, problem.getPoints());
            ps.setInt(9, problem.getTimeLimit());
            ps.setString(10, problem.getSampleInput());
            ps.setString(11, problem.getSampleOutput());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int generatedId = rs.getInt(1);
                        problem.setProblemId(generatedId);
                        // Save sample test case automatically
                        if (problem.getSampleInput() != null && !problem.getSampleInput().isEmpty()) {
                            testCaseDAO.addTestCase(new TestCase(generatedId, problem.getSampleInput(), problem.getSampleOutput(), true));
                        }
                        return generatedId;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding problem", e);
        }
        return -1;
    }

    public boolean updateProblem(Problem problem) {
        String sql = """
            UPDATE problems SET title = ?, description = ?, input_format = ?, output_format = ?, constraints = ?,
            difficulty = ?, category = ?, points = ?, time_limit = ?, sample_input = ?, sample_output = ?
            WHERE problem_id = ?
        """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, problem.getTitle());
            ps.setString(2, problem.getDescription());
            ps.setString(3, problem.getInputFormat());
            ps.setString(4, problem.getOutputFormat());
            ps.setString(5, problem.getConstraints());
            ps.setString(6, problem.getDifficulty().name());
            ps.setString(7, problem.getCategory().name());
            ps.setInt(8, problem.getPoints());
            ps.setInt(9, problem.getTimeLimit());
            ps.setString(10, problem.getSampleInput());
            ps.setString(11, problem.getSampleOutput());
            ps.setInt(12, problem.getProblemId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating problem", e);
            return false;
        }
    }

    public boolean deleteProblem(int problemId) {
        String sql = "DELETE FROM problems WHERE problem_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, problemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting problem", e);
            return false;
        }
    }

    public List<Problem> getProblemsByBattleId(int battleId) {
        List<Problem> list = new ArrayList<>();
        String sql = """
            SELECT p.* FROM problems p
            JOIN battle_problems bp ON p.problem_id = bp.problem_id
            WHERE bp.battle_id = ?
            ORDER BY p.problem_id ASC
        """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, battleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Problem p = mapRowToProblem(rs);
                    p.setTestCases(testCaseDAO.getTestCasesByProblemId(p.getProblemId()));
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching battle problems", e);
        }
        return list;
    }

    public Set<Integer> getSolvedProblemIdsForUser(int userId) {
        Set<Integer> solvedIds = new HashSet<>();
        String sql = "SELECT DISTINCT problem_id FROM submissions WHERE user_id = ? AND status = 'ACCEPTED'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    solvedIds.add(rs.getInt("problem_id"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching solved problem IDs", e);
        }
        return solvedIds;
    }

    private Problem mapRowToProblem(ResultSet rs) throws SQLException {
        return new Problem(
                rs.getInt("problem_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("input_format"),
                rs.getString("output_format"),
                rs.getString("constraints"),
                Difficulty.fromString(rs.getString("difficulty")),
                Category.fromString(rs.getString("category")),
                rs.getInt("points"),
                rs.getInt("time_limit"),
                rs.getString("sample_input"),
                rs.getString("sample_output")
        );
    }
}
