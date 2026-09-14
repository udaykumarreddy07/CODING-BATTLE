package com.codingbattle.dao;

import com.codingbattle.database.DatabaseConnection;
import com.codingbattle.model.TestCase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TestCase Data Access Object for CRUD operations on problem test cases.
 */
public class TestCaseDAO {

    private static final Logger LOGGER = Logger.getLogger(TestCaseDAO.class.getName());

    public List<TestCase> getTestCasesByProblemId(int problemId) {
        List<TestCase> list = new ArrayList<>();
        String sql = "SELECT test_id, problem_id, input_data, expected_output, is_sample FROM test_cases WHERE problem_id = ? ORDER BY is_sample DESC, test_id ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, problemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TestCase(
                            rs.getInt("test_id"),
                            rs.getInt("problem_id"),
                            rs.getString("input_data"),
                            rs.getString("expected_output"),
                            rs.getBoolean("is_sample")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching test cases for problem " + problemId, e);
        }
        return list;
    }

    public boolean addTestCase(TestCase testCase) {
        String sql = "INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, testCase.getProblemId());
            ps.setString(2, testCase.getInputData());
            ps.setString(3, testCase.getExpectedOutput());
            ps.setBoolean(4, testCase.isSample());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        testCase.setTestId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding test case", e);
        }
        return false;
    }

    public boolean updateTestCase(TestCase testCase) {
        String sql = "UPDATE test_cases SET input_data = ?, expected_output = ?, is_sample = ? WHERE test_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, testCase.getInputData());
            ps.setString(2, testCase.getExpectedOutput());
            ps.setBoolean(3, testCase.isSample());
            ps.setInt(4, testCase.getTestId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating test case", e);
            return false;
        }
    }

    public boolean deleteTestCase(int testId) {
        String sql = "DELETE FROM test_cases WHERE test_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, testId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting test case", e);
            return false;
        }
    }
}
