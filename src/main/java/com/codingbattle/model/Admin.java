package com.codingbattle.model;

import java.time.LocalDateTime;

/**
 * Admin class extending User.
 * Demonstrates Inheritance and Polymorphism.
 */
public class Admin extends User {
    private String department;

    public Admin() {
        super();
        setRole("ADMIN");
    }

    public Admin(int userId, String username, String email, String password, LocalDateTime createdAt, String department) {
        super(userId, username, email, password, "ADMIN", createdAt);
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String getRoleDescription() {
        return "System Administrator: Full privilege to manage problems, test cases, battles, students, and audits.";
    }
}
