package com.codingbattle;

import com.codingbattle.database.DatabaseConnection;
import com.codingbattle.database.DatabaseInitializer;
import com.codingbattle.model.Problem;
import com.codingbattle.model.Student;
import com.codingbattle.model.User;
import com.codingbattle.service.ProblemService;
import com.codingbattle.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseTest {

    @BeforeAll
    public static void setup() {
        DatabaseInitializer.initialize();
    }

    @Test
    public void testDatabaseConnection() throws Exception {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            assertNotNull(conn, "Connection should not be null");
            assertFalse(conn.isClosed(), "Connection should be open");
        }
    }

    @Test
    public void testSeedUsers() {
        UserService service = new UserService();
        User admin = service.login("admin", "admin123");
        assertNotNull(admin, "Admin should be present in database");
        assertEquals("ADMIN", admin.getRole());

        User student = service.login("rahul", "student123");
        assertNotNull(student, "Rahul should be present in database");
        assertEquals("STUDENT", student.getRole());
    }

    @Test
    public void testSeedProblems() {
        ProblemService service = new ProblemService();
        List<Problem> problems = service.getAllProblems();
        assertFalse(problems.isEmpty(), "Problems should be seeded");
        assertTrue(problems.size() >= 5, "At least 5 problems should be present");

        Problem twoSum = service.getProblemDetails(1);
        assertNotNull(twoSum);
        assertEquals("Two Sum", twoSum.getTitle());
        assertFalse(twoSum.getTestCases().isEmpty(), "Two Sum should have test cases");
    }
}
