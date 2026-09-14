package com.codingbattle.service;

import com.codingbattle.dao.UserDAO;
import com.codingbattle.model.Student;
import com.codingbattle.model.User;
import com.codingbattle.util.ValidationUtil;

import java.util.List;

/**
 * Service Layer for User Management and Authentication.
 */
public class UserService {

    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User login(String username, String password) throws IllegalArgumentException {
        if (!ValidationUtil.isNotEmpty(username) || !ValidationUtil.isNotEmpty(password)) {
            throw new IllegalArgumentException("Username and Password must not be empty.");
        }
        User user = userDAO.authenticate(username.trim(), password.trim());
        if (user == null) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
        return user;
    }

    public void registerStudent(String username, String email, String password) throws IllegalArgumentException {
        if (!ValidationUtil.isValidUsername(username)) {
            throw new IllegalArgumentException("Username must be 3-20 characters alphanumeric (letters, digits, underscore).");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (!ValidationUtil.isValidPassword(password)) {
            throw new IllegalArgumentException("Password must be at least 4 characters.");
        }
        if (userDAO.isUsernameTaken(username.trim())) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }
        if (userDAO.isEmailTaken(email.trim())) {
            throw new IllegalArgumentException("Email '" + email + "' is already registered.");
        }

        boolean success = userDAO.register(username.trim(), email.trim(), password.trim(), "STUDENT");
        if (!success) {
            throw new RuntimeException("Database error during registration. Please try again.");
        }
    }

    public Student getStudentProfile(int userId) {
        return userDAO.getStudentById(userId);
    }

    public List<Student> getAllStudents() {
        return userDAO.getAllStudents();
    }

    public boolean removeStudent(int userId) {
        return userDAO.deleteUser(userId);
    }
}
