package com.codingbattle.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database Initializer that creates tables and seeds initial data.
 * Compatible with both MySQL and H2 database engines.
 */
public class DatabaseInitializer {

    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());

    public static synchronized void initialize() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            LOGGER.info("Initializing database schema on: " + DatabaseConnection.getInstance().getActiveDatabaseType());

            // 1. Users Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    email VARCHAR(100) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // 2. Problems Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS problems (
                    problem_id INT AUTO_INCREMENT PRIMARY KEY,
                    title VARCHAR(150) NOT NULL,
                    description TEXT NOT NULL,
                    input_format TEXT,
                    output_format TEXT,
                    constraints TEXT,
                    difficulty VARCHAR(20) NOT NULL,
                    category VARCHAR(50) NOT NULL,
                    points INT DEFAULT 100,
                    time_limit INT DEFAULT 2000,
                    sample_input TEXT,
                    sample_output TEXT
                )
            """);

            // 3. Test Cases Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS test_cases (
                    test_id INT AUTO_INCREMENT PRIMARY KEY,
                    problem_id INT NOT NULL,
                    input_data TEXT NOT NULL,
                    expected_output TEXT NOT NULL,
                    is_sample BOOLEAN DEFAULT FALSE,
                    FOREIGN KEY (problem_id) REFERENCES problems(problem_id) ON DELETE CASCADE
                )
            """);

            // 4. Battles Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS battles (
                    battle_id INT AUTO_INCREMENT PRIMARY KEY,
                    battle_name VARCHAR(150) NOT NULL,
                    start_time TIMESTAMP NULL,
                    end_time TIMESTAMP NULL,
                    duration INT DEFAULT 30,
                    status VARCHAR(20) NOT NULL DEFAULT 'UPCOMING',
                    max_participants INT DEFAULT 100
                )
            """);

            // 5. Battle Problems Mapping Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS battle_problems (
                    battle_id INT NOT NULL,
                    problem_id INT NOT NULL,
                    PRIMARY KEY (battle_id, problem_id),
                    FOREIGN KEY (battle_id) REFERENCES battles(battle_id) ON DELETE CASCADE,
                    FOREIGN KEY (problem_id) REFERENCES problems(problem_id) ON DELETE CASCADE
                )
            """);

            // 6. Submissions Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS submissions (
                    submission_id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    problem_id INT NOT NULL,
                    battle_id INT NULL,
                    source_code TEXT NOT NULL,
                    status VARCHAR(30) NOT NULL,
                    score INT DEFAULT 0,
                    execution_time BIGINT DEFAULT 0,
                    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                    FOREIGN KEY (problem_id) REFERENCES problems(problem_id) ON DELETE CASCADE
                )
            """);

            // 7. Scores Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS scores (
                    score_id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    battle_id INT NOT NULL,
                    total_score INT DEFAULT 0,
                    rank_position INT DEFAULT 0,
                    problems_solved INT DEFAULT 0,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                    FOREIGN KEY (battle_id) REFERENCES battles(battle_id) ON DELETE CASCADE
                )
            """);

            // 8. Achievements Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS achievements (
                    achievement_id INT AUTO_INCREMENT PRIMARY KEY,
                    badge_code VARCHAR(50) NOT NULL UNIQUE,
                    name VARCHAR(100) NOT NULL,
                    description VARCHAR(255) NOT NULL,
                    icon VARCHAR(50) NOT NULL,
                    points_reward INT DEFAULT 100
                )
            """);

            // 9. User Achievements Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS user_achievements (
                    user_id INT NOT NULL,
                    achievement_id INT NOT NULL,
                    earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (user_id, achievement_id),
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                    FOREIGN KEY (achievement_id) REFERENCES achievements(achievement_id) ON DELETE CASCADE
                )
            """);

            // Seed initial data if tables are empty
            seedInitialData(conn);

            LOGGER.info("Database schema verification and seed complete.");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database initialization failed", e);
        }
    }

    private static void seedInitialData(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Check if users exist
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) {
                LOGGER.info("Seeding initial users...");
                stmt.execute("""
                    INSERT INTO users (username, email, password, role) VALUES
                    ('admin', 'admin@codingbattle.edu', 'admin123', 'ADMIN'),
                    ('rahul', 'rahul@student.edu', 'student123', 'STUDENT'),
                    ('ananya', 'ananya@student.edu', 'student123', 'STUDENT'),
                    ('kiran', 'kiran@student.edu', 'student123', 'STUDENT'),
                    ('uday', 'uday@student.edu', 'student123', 'STUDENT'),
                    ('priya', 'priya@student.edu', 'student123', 'STUDENT')
                """);
            }

            // Check if achievements exist
            rs = stmt.executeQuery("SELECT COUNT(*) FROM achievements");
            if (rs.next() && rs.getInt(1) == 0) {
                LOGGER.info("Seeding badges...");
                stmt.execute("""
                    INSERT INTO achievements (badge_code, name, description, icon, points_reward) VALUES
                    ('FIRST_BLOOD', 'First Solution', 'Solved your first coding problem', 'medal.png', 100),
                    ('STREAK_5', '5-Day Streak', 'Maintained a 5-day coding streak', 'fire.png', 250),
                    ('SPEED_DEMON', 'Speed Coder', 'Solved a problem in under 250ms', 'bolt.png', 150),
                    ('TEN_PROBLEMS', '10 Problems Solved', 'Milestone: 10 problems completed', 'target.png', 300),
                    ('BATTLE_WINNER', 'Battle Winner', 'Secured 1st rank in a coding battle', 'trophy.png', 500),
                    ('TOP_TEN', 'Top 10 Coder', 'Reached the top 10 on the leaderboard', 'crown.png', 400),
                    ('FIFTY_PROBLEMS', '50 Problems Solved', 'Grandmaster milestone reached', 'rocket.png', 1000)
                """);
            }

            // Check if problems exist
            rs = stmt.executeQuery("SELECT COUNT(*) FROM problems");
            if (rs.next() && rs.getInt(1) == 0) {
                LOGGER.info("Seeding curated algorithmic problems and test cases...");

                // Problem 1: Two Sum
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (1, 'Two Sum',
                    'Given an array of integers and a target value, find two numbers whose sum equals the target. Print their 0-based indices separated by a space in ascending order.',
                    'Line 1: An integer N representing the array size.\\nLine 2: N space-separated integers.\\nLine 3: An integer Target.',
                    'Two space-separated indices of the two numbers whose sum equals target.',
                    '2 <= N <= 10^4\\n-10^9 <= nums[i] <= 10^9',
                    'EASY', 'ARRAYS', 100, 2000,
                    '5\\n2 7 11 15 3\\n9',
                    '0 1')
                """);

                // Test cases for Two Sum
                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (1, '5\n2 7 11 15 3\n9', '0 1', true),
                    (1, '4\n3 2 4 8\n6', '1 2', false),
                    (1, '3\n3 3 1\n6', '0 1', false),
                    (1, '4\n-1 -2 -3 -4\n-5', '1 2', false)
                """);

                // Problem 2: Palindrome Number
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (2, 'Palindrome Number',
                    'Given an integer x, print true if x is a palindrome, and false otherwise. An integer is a palindrome when it reads the same backward as forward.',
                    'A single integer x.',
                    'true or false',
                    '-2^31 <= x <= 2^31 - 1',
                    'EASY', 'MATHEMATICS', 100, 2000,
                    '121',
                    'true')
                """);

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (2, '121', 'true', true),
                    (2, '-121', 'false', false),
                    (2, '10', 'false', false),
                    (2, '12321', 'true', false)
                """);

                // Problem 3: Valid Parentheses
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (3, 'Valid Parentheses',
                    'Given a string containing just the characters ''('', '')'', ''{'', ''}'', ''['' and '']'', determine if the input string is valid. Open brackets must be closed by the same type of brackets and in the correct order.',
                    'A single line containing the bracket string.',
                    'true or false',
                    '1 <= s.length <= 10^4',
                    'EASY', 'STACK', 100, 2000,
                    '()[]{}',
                    'true')
                """);

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (3, '()[]{}', 'true', true),
                    (3, '(]', 'false', false),
                    (3, '([)]', 'false', false),
                    (3, '{[]}', 'true', false)
                """);

                // Problem 4: Reverse String
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (4, 'Reverse String',
                    'Write a program that takes a string input and prints the string reversed.',
                    'A single word or string.',
                    'The reversed string.',
                    '1 <= length <= 10^5',
                    'EASY', 'STRINGS', 100, 2000,
                    'hello',
                    'olleh')
                """);

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (4, 'hello', 'olleh', true),
                    (4, 'battle', 'elttab', false),
                    (4, 'java', 'avaj', false)
                """);

                // Problem 5: Maximum Subarray
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (5, 'Maximum Subarray (Kadane Algorithm)',
                    'Given an integer array nums, find the subarray with the largest sum, and return its sum.',
                    'Line 1: Integer N\\nLine 2: N space-separated integers',
                    'A single integer representing the maximum subarray sum.',
                    '1 <= N <= 10^5',
                    'MEDIUM', 'DYNAMIC_PROGRAMMING', 200, 2000,
                    '9\\n-2 1 -3 4 -1 2 1 -5 4',
                    '6')
                """);

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (5, '9\n-2 1 -3 4 -1 2 1 -5 4', '6', true),
                    (5, '1\n1', '1', false),
                    (5, '5\n5 4 -1 7 8', '23', false),
                    (5, '4\n-1 -2 -3 -4', '-1', false)
                """);

                // Problem 6: Binary Search
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (6, 'Binary Search',
                    'Given an array of integers nums which is sorted in ascending order, and an integer target, write a function to search target in nums. If target exists, then return its index. Otherwise, return -1.',
                    'Line 1: Array length N\\nLine 2: N sorted integers\\nLine 3: Target integer',
                    'Index of target or -1 if not found.',
                    '1 <= N <= 10^4',
                    'EASY', 'SEARCHING', 100, 2000,
                    '6\\n-1 0 3 5 9 12\\n9',
                    '4')
                """);

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (6, '6\n-1 0 3 5 9 12\n9', '4', true),
                    (6, '6\n-1 0 3 5 9 12\n2', '-1', false),
                    (6, '1\n5\n5', '0', false)
                """);

                // Problem 7: Fibonacci Number
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (7, 'Nth Fibonacci Number',
                    'The Fibonacci numbers form a sequence where F(0) = 0, F(1) = 1, and F(n) = F(n-1) + F(n-2) for n > 1. Given n, calculate F(n).',
                    'An integer n.',
                    'F(n)',
                    '0 <= n <= 30',
                    'EASY', 'RECURSION', 100, 2000,
                    '6',
                    '8')
                """);

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (7, '6', '8', true),
                    (7, '2', '1', false),
                    (7, '3', '2', false),
                    (7, '10', '55', false)
                """);

                // Problem 8: Longest Substring Without Repeating Characters
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (8, 'Longest Substring Without Repeating Characters',
                    'Given a string s, find the length of the longest substring without repeating characters.',
                    'A string s.',
                    'Integer length of the longest substring.',
                    '0 <= s.length <= 5 * 10^4',
                    'MEDIUM', 'STRINGS', 200, 2500,
                    'abcabcbb',
                    '3')
                """);

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (8, 'abcabcbb', '3', true),
                    (8, 'bbbbb', '1', false),
                    (8, 'pwwkew', '3', false),
                    (8, ' ', '1', false)
                """);
            }

            // Check if Battles exist
            rs = stmt.executeQuery("SELECT COUNT(*) FROM battles");
            if (rs.next() && rs.getInt(1) == 0) {
                LOGGER.info("Seeding coding battles...");
                stmt.execute("""
                    INSERT INTO battles (battle_id, battle_name, start_time, end_time, duration, status, max_participants) VALUES
                    (1, 'Java Speed Battle #05', CURRENT_TIMESTAMP, DATEADD('MINUTE', 45, CURRENT_TIMESTAMP), 45, 'LIVE', 50),
                    (2, 'DSA Campus Cup 2026', DATEADD('DAY', 1, CURRENT_TIMESTAMP), DATEADD('DAY', 1, DATEADD('MINUTE', 60, CURRENT_TIMESTAMP)), 60, 'UPCOMING', 100),
                    (3, 'Freshers Sprint Contest', DATEADD('DAY', -2, CURRENT_TIMESTAMP), DATEADD('DAY', -2, DATEADD('MINUTE', 30, CURRENT_TIMESTAMP)), 30, 'COMPLETED', 40)
                """);

                // Map problems to battle 1 (Live Battle)
                stmt.execute("""
                    INSERT INTO battle_problems (battle_id, problem_id) VALUES
                    (1, 1),
                    (1, 2),
                    (1, 3),
                    (1, 4),
                    (1, 5)
                """);
            }

            // Seed initial submissions & achievements for active demo
            rs = stmt.executeQuery("SELECT COUNT(*) FROM submissions");
            if (rs.next() && rs.getInt(1) == 0) {
                LOGGER.info("Seeding realistic student scores and achievements...");

                // Rahul solved problems 1, 2, 3, 4, 6
                stmt.execute("""
                    INSERT INTO submissions (user_id, problem_id, battle_id, source_code, status, score, execution_time) VALUES
                    (2, 1, 1, '// Solved Two Sum', 'ACCEPTED', 110, 85),
                    (2, 2, 1, '// Solved Palindrome Number', 'ACCEPTED', 100, 92),
                    (2, 3, NULL, '// Solved Valid Parentheses', 'ACCEPTED', 100, 110),
                    (2, 4, NULL, '// Solved Reverse String', 'ACCEPTED', 100, 75),
                    (2, 6, NULL, '// Solved Binary Search', 'ACCEPTED', 100, 68),
                    (3, 1, 1, '// Ananya solution', 'ACCEPTED', 100, 140),
                    (3, 2, 1, '// Ananya solution', 'ACCEPTED', 100, 120),
                    (3, 5, NULL, '// Ananya Kadane', 'ACCEPTED', 200, 190),
                    (4, 1, 1, '// Kiran solution', 'ACCEPTED', 100, 150),
                    (5, 1, 1, '// Uday solution', 'ACCEPTED', 100, 95)
                """);

                // Seed scores for battle 1
                stmt.execute("""
                    INSERT INTO scores (user_id, battle_id, total_score, rank_position, problems_solved) VALUES
                    (2, 1, 210, 1, 2),
                    (3, 1, 200, 2, 2),
                    (5, 1, 100, 3, 1),
                    (4, 1, 100, 4, 1)
                """);

                // Award initial badges
                stmt.execute("""
                    INSERT INTO user_achievements (user_id, achievement_id) VALUES
                    (2, 1),
                    (2, 3),
                    (3, 1)
                """);
            }
        }
    }
}
