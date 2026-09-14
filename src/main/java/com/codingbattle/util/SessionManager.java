package com.codingbattle.util;

import com.codingbattle.model.Admin;
import com.codingbattle.model.Student;
import com.codingbattle.model.User;

/**
 * Global Session Manager tracking the currently authenticated user.
 */
public class SessionManager {

    private static User currentUser;

    public static synchronized void setCurrentUser(User user) {
        currentUser = user;
    }

    public static synchronized User getCurrentUser() {
        return currentUser;
    }

    public static synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public static synchronized boolean isStudent() {
        return currentUser instanceof Student;
    }

    public static synchronized boolean isAdmin() {
        return currentUser instanceof Admin;
    }

    public static synchronized Student getStudent() {
        if (currentUser instanceof Student) {
            return (Student) currentUser;
        }
        return null;
    }

    public static synchronized Admin getAdmin() {
        if (currentUser instanceof Admin) {
            return (Admin) currentUser;
        }
        return null;
    }

    public static synchronized void logout() {
        currentUser = null;
    }
}
