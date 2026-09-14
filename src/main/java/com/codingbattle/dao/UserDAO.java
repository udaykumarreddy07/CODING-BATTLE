package com.codingbattle.dao;

import com.codingbattle.database.DatabaseConnection;
import com.codingbattle.model.Admin;
import com.codingbattle.model.Student;
import com.codingbattle.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User Data Access Object handling database operations for Users, Students, and Admins.
 * Uses PreparedStatements for all operations to prevent SQL Injection.
 */
public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    public User authenticate(String username, String password) {
        String sql = "SELECT user_id, username, email, password, role, created_at FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("user_id");
                    String uname = rs.getString("username");
                    String email = rs.getString("email");
                    String pass = rs.getString("password");
                    String role = rs.getString("role");
                    Timestamp ts = rs.getTimestamp("created_at");
                    LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

                    if ("ADMIN".equalsIgnoreCase(role)) {
                        return new Admin(id, uname, email, pass, createdAt, "Computer Science & Engineering");
                    } else {
                        Student student = new Student(id, uname, email, pass, createdAt);
                        enrichStudentStats(conn, student);
                        return student;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Authentication query error", e);
        }
        return null;
    }

    public boolean register(String username, String email, String password, String role) {
        String sql = "INSERT INTO users (username, email, password, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, role != null ? role.toUpperCase() : "STUDENT");

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "User registration failed", e);
            return false;
        }
    }

    public boolean isUsernameTaken(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Username check error", e);
            return false;
        }
    }

    public boolean isEmailTaken(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Email check error", e);
            return false;
        }
    }

    public Student getStudentById(int userId) {
        String sql = "SELECT user_id, username, email, password, created_at FROM users WHERE user_id = ? AND role = 'STUDENT'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student s = new Student(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : LocalDateTime.now()
                    );
                    enrichStudentStats(conn, s);
                    return s;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getStudentById error", e);
        }
        return null;
    }

    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT user_id, username, email, password, created_at FROM users WHERE role = 'STUDENT' ORDER BY user_id ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Student s = new Student(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : LocalDateTime.now()
                );
                enrichStudentStats(conn, s);
                list.add(s);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getAllStudents error", e);
        }
        return list;
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "deleteUser error", e);
            return false;
        }
    }

    private void enrichStudentStats(Connection conn, Student student) {
        // Compute total points & solved problems
        String subSql = "SELECT COALESCE(SUM(score), 0) as total_pts, COUNT(DISTINCT problem_id) as solved " +
                "FROM submissions WHERE user_id = ? AND status = 'ACCEPTED'";
        try (PreparedStatement ps = conn.prepareStatement(subSql)) {
            ps.setInt(1, student.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    student.setTotalPoints(rs.getInt("total_pts"));
                    student.setSolvedProblems(rs.getInt("solved"));
                }
            }
        } catch (SQLException ignored) {}

        // Battles count
        String battleSql = "SELECT COUNT(DISTINCT battle_id) FROM scores WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(battleSql)) {
            ps.setInt(1, student.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    student.setBattlesParticipated(rs.getInt(1));
                }
            }
        } catch (SQLException ignored) {}
    }
}
