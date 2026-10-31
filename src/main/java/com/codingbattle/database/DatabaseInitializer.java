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
                LOGGER.info("Seeding curated CSE201 syllabus practice problems and test cases...");

                // Problem 1: Unit 1: Classes & Objects (Student Profile)
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (1, 'Unit 1: Classes & Objects (Student Profile)',
                    'In Object-Oriented Programming, a Class defines the blueprint and an Object represents a concrete instance. Define a Student class with private fields for roll number (String), name (String), and three subject marks (int). Implement getter methods, a method to compute average marks, and determine if the student PASSED (average >= 40.0) or FAILED.',
                    'Line 1: Roll number
Line 2: Student name
Line 3: Three space-separated marks (integers)',
                    'Roll: <roll> | Name: <name> | Average: <avg to 1 decimal place> | Result: <PASS/FAIL>',
                    'Marks are integers between 0 and 100',
                    'EASY', 'UNIT_1_OOPS', 100, 2000,
                    'CSE-101
Alice Smith
85 90 80',
                    'Roll: CSE-101 | Name: Alice Smith | Average: 85.0 | Result: PASS')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (1, 'CSE-101
Alice Smith
85 90 80', 'Roll: CSE-101 | Name: Alice Smith | Average: 85.0 | Result: PASS', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (1, 'CSE-102
Bob Jones
30 35 42', 'Roll: CSE-102 | Name: Bob Jones | Average: 35.7 | Result: FAIL', false)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (1, 'CSE-103
Charlie Brown
40 40 40', 'Roll: CSE-103 | Name: Charlie Brown | Average: 40.0 | Result: PASS', false)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (1, 'IT-204
Diana Prince
98 95 100', 'Roll: IT-204 | Name: Diana Prince | Average: 97.7 | Result: PASS', false)
                """);
        

                // Problem 2: Unit 1: Java Basics & Explicit/Implicit Type Casting
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (2, 'Unit 1: Java Basics & Explicit/Implicit Type Casting',
                    'Type casting in Java occurs in two ways: Implicit (widening) where a smaller data type is automatically converted to a larger type, and Explicit (narrowing) where a larger type is manually cast into a smaller type. Given a double d and an integer n, perform: (1) Explicit narrowing of d to integer, (2) Implicit widening of n to double (formatted to 1 decimal place), and (3) Product of d and n formatted to 2 decimal places.',
                    'A double value d and an integer n on a single line separated by a space.',
                    'Three lines:
Narrowed to int: <int>
Widened to double: <double>
Product: <product>',
                    '-10000.0 <= d <= 10000.0, 1 <= n <= 1000',
                    'EASY', 'UNIT_1_OOPS', 100, 2000,
                    '45.75 4',
                    'Narrowed to int: 45
Widened to double: 4.0
Product: 183.00')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (2, '45.75 4', 'Narrowed to int: 45
Widened to double: 4.0
Product: 183.00', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (2, '12.345 10', 'Narrowed to int: 12
Widened to double: 10.0
Product: 123.45', false)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (2, '-9.8 5', 'Narrowed to int: -9
Widened to double: 5.0
Product: -49.00', false)
                """);
        

                // Problem 3: Unit 1: Control Flow & Academic Grade Evaluator
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (3, 'Unit 1: Control Flow & Academic Grade Evaluator',
                    'Control flow allows branching and conditional execution. Given a student''s marks m (0 to 100), evaluate the letter grade and remarks using if-else / switch conditions:
>= 90: Grade S, Remarks: Excellent
>= 80: Grade A, Remarks: Very Good
>= 70: Grade B, Remarks: Good
>= 60: Grade C, Remarks: Satisfactory
>= 50: Grade D, Remarks: Pass
< 50: Grade F, Remarks: Needs Improvement',
                    'A single integer m representing student marks.',
                    'Grade: <grade> | Remarks: <remarks>',
                    '0 <= m <= 100',
                    'EASY', 'UNIT_1_OOPS', 100, 2000,
                    '85',
                    'Grade: A | Remarks: Very Good')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (3, '85', 'Grade: A | Remarks: Very Good', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (3, '95', 'Grade: S | Remarks: Excellent', false)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (3, '45', 'Grade: F | Remarks: Needs Improvement', false)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (3, '62', 'Grade: C | Remarks: Satisfactory', false)
                """);
        

                // Problem 4: Unit 1: 1D & 2D Arrays (Matrix Transpose)
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (4, 'Unit 1: 1D & 2D Arrays (Matrix Transpose)',
                    'An array in Java is a dynamically created object that holds elements of the same type. Given a 2D matrix of R rows and C columns, construct its transpose (swapping row and column indices so that element at (i, j) moves to (j, i)) and print the resulting C x R matrix.',
                    'Line 1: Two space-separated integers R and C.
Next R lines: C space-separated integers.',
                    'C lines, each containing R space-separated integers representing the transposed matrix.',
                    '1 <= R, C <= 50',
                    'EASY', 'UNIT_1_OOPS', 100, 2000,
                    '2 3
1 2 3
4 5 6',
                    '1 4
2 5
3 6')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (4, '2 3
1 2 3
4 5 6', '1 4
2 5
3 6', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (4, '3 3
1 0 0
0 1 0
0 0 1', '1 0 0
0 1 0
0 0 1', false)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (4, '1 4
10 20 30 40', '10
20
30
40', false)
                """);
        

                // Problem 5: Unit 1: Constructors & Constructor Overloading (Box Dimensions)
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (5, 'Unit 1: Constructors & Constructor Overloading (Box Dimensions)',
                    'A Constructor is a special method used to initialize objects. Create a Box class featuring constructor overloading: (1) Default constructor that sets length=1, width=1, height=1, and (2) Parameterized constructor Box(int l, int w, int h). Provide methods to calculate Volume (l * w * h) and Surface Area (2 * (l*w + w*h + h*l)).',
                    'Either ''DEFAULT'' or ''PARAM <l> <w> <h>''',
                    'Box [L: <l>, W: <w>, H: <h>] | Volume: <vol> | Surface Area: <area>',
                    '1 <= l, w, h <= 500',
                    'EASY', 'UNIT_1_OOPS', 100, 2000,
                    'DEFAULT',
                    'Box [L: 1, W: 1, H: 1] | Volume: 1 | Surface Area: 6')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (5, 'DEFAULT', 'Box [L: 1, W: 1, H: 1] | Volume: 1 | Surface Area: 6', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (5, 'PARAM 3 4 5', 'Box [L: 3, W: 4, H: 5] | Volume: 60 | Surface Area: 94', false)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (5, 'PARAM 10 10 10', 'Box [L: 10, W: 10, H: 10] | Volume: 1000 | Surface Area: 600', false)
                """);
        

                // Problem 6: Unit 1: Methods & Method Calling (Call by Value & Reference)
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (6, 'Unit 1: Methods & Method Calling (Call by Value & Reference)',
                    'Methods encapsulate reusable behavior. In Java, all primitive arguments are passed by value, whereas objects and arrays pass object reference values. Read an array of N integers and a multiplier scalar K. Implement a static method ''scaleArray(int[] arr, int k)'' that modifies the array in-place, and a method ''sumArray(int[] arr)'' that computes the sum.',
                    'Line 1: N
Line 2: N space-separated integers
Line 3: Multiplier integer K',
                    'Line 1: Original: <space-separated original elements>
Line 2: Scaled: <space-separated scaled elements>
Line 3: Sum: <sum of scaled elements>',
                    '1 <= N <= 1000, 1 <= K <= 100',
                    'EASY', 'UNIT_1_OOPS', 100, 2000,
                    '4
2 4 6 8
3',
                    'Original: 2 4 6 8
Scaled: 6 12 18 24
Sum: 60')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (6, '4
2 4 6 8
3', 'Original: 2 4 6 8
Scaled: 6 12 18 24
Sum: 60', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (6, '3
1 1 1
5', 'Original: 1 1 1
Scaled: 5 5 5
Sum: 15', false)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (6, '1
10
2', 'Original: 10
Scaled: 20
Sum: 20', false)
                """);
        

                // Problem 7: Unit 1: Static Members & Class-Level Counter
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (7, 'Unit 1: Static Members & Class-Level Counter',
                    'Static variables belong to the class rather than individual instances, shared by all objects. Design a Product class with a static variable ''totalRegistered'' and static variable ''totalRevenue''. Every instantiated Product increments the counter and accumulates price. After reading N products, display the class-level totals and the average price.',
                    'Line 1: N (number of products)
Next N lines: Product name and price (double)',
                    'Three lines:
Total Products Registered: <N>
Total Revenue: <revenue to 2 decimal places>
Average Price: <avg to 2 decimal places>',
                    '1 <= N <= 1000',
                    'EASY', 'UNIT_1_OOPS', 100, 2000,
                    '3
Laptop 850.50
Mouse 25.00
Keyboard 45.25',
                    'Total Products Registered: 3
Total Revenue: 920.75
Average Price: 306.92')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (7, '3
Laptop 850.50
Mouse 25.00
Keyboard 45.25', 'Total Products Registered: 3
Total Revenue: 920.75
Average Price: 306.92', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (7, '1
Book 15.50', 'Total Products Registered: 1
Total Revenue: 15.50
Average Price: 15.50', false)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (7, '2
Monitor 200.00
Desk 150.00', 'Total Products Registered: 2
Total Revenue: 350.00
Average Price: 175.00', false)
                """);
        

                // Problem 8: Unit 2: Method Overloading (Compile-Time Polymorphism)
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (8, 'Unit 2: Method Overloading (Compile-Time Polymorphism)',
                    'Method Overloading allows multiple methods in the same class to share the same name with different parameter signatures. Implement an overloaded calculateArea method:
- calculateArea(double r): Area of circle = Math.PI * r * r
- calculateArea(double w, double h): Area of rectangle = w * h
- calculateArea(int b, int h): Area of triangle = 0.5 * b * h
Read shape command and display the area formatted to 2 decimal places.',
                    'One line with: ''CIRCLE <r>'' or ''RECTANGLE <w> <h>'' or ''TRIANGLE <b> <h>''',
                    'Shape: <SHAPE> | Area: <area formatted to 2 decimals>',
                    'Dimensions are positive numbers <= 1000',
                    'EASY', 'UNIT_2_INHERITANCE', 100, 2000,
                    'CIRCLE 7.0',
                    'Shape: CIRCLE | Area: 153.94')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (8, 'CIRCLE 7.0', 'Shape: CIRCLE | Area: 153.94', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (8, 'RECTANGLE 4.5 8.0', 'Shape: RECTANGLE | Area: 36.00', false)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (8, 'TRIANGLE 6 9', 'Shape: TRIANGLE | Area: 27.00', false)
                """);
        

                // Problem 9: Unit 2: Single & Multilevel Inheritance (Super Keyword)
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (9, 'Unit 2: Single & Multilevel Inheritance (Super Keyword)',
                    'Inheritance enables a subclass to inherit attributes and behaviors from a superclass using the ''extends'' keyword. Demonstrate multilevel inheritance:
1. Superclass ''Person'' (name, age)
2. Subclass ''Employee'' extends Person (empId, baseSalary) using super(name, age)
3. Subclass ''Manager'' extends Employee (department, bonus) using super(name, age, empId, baseSalary)
Calculate total compensation (baseSalary + bonus) and display manager info.',
                    'A single line: <name> <age> <empId> <baseSalary> <department> <bonus>',
                    'Manager: <name> (ID: <empId>, Age: <age>) | Dept: <department> | Total Compensation: <total>',
                    'Age > 0, Salary > 0',
                    'MEDIUM', 'UNIT_2_INHERITANCE', 150, 2000,
                    'Sarah 35 EMP401 75000 Engineering 15000',
                    'Manager: Sarah (ID: EMP401, Age: 35) | Dept: Engineering | Total Compensation: 90000')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (9, 'Sarah 35 EMP401 75000 Engineering 15000', 'Manager: Sarah (ID: EMP401, Age: 35) | Dept: Engineering | Total Compensation: 90000', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (9, 'Vikram 42 MGR102 120000 Finance 25000', 'Manager: Vikram (ID: MGR102, Age: 42) | Dept: Finance | Total Compensation: 145000', false)
                """);
        

                // Problem 10: Unit 2: Method Overriding & Dynamic Method Dispatch
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (10, 'Unit 2: Method Overriding & Dynamic Method Dispatch',
                    'Dynamic Method Dispatch is the mechanism by which a call to an overridden method is resolved at runtime rather than compile-time. Create a base class ''Vehicle'' with method ''describe()''. Derive subclasses ''Car'' and ''Bike'' which override ''describe()''. Store vehicles in an array of Vehicle references and invoke describe() polymorphically.',
                    'Line 1: N (count of vehicles)
Next N lines: Vehicle type (''CAR'' or ''BIKE''), Model name, TopSpeed',
                    'N lines of dispatched descriptions:
[Vehicle Dispatch] CAR <model> -> Max Speed: <topSpeed> km/h
or [Vehicle Dispatch] BIKE <model> -> Max Speed: <topSpeed> km/h',
                    '1 <= N <= 100',
                    'MEDIUM', 'UNIT_2_INHERITANCE', 150, 2000,
                    '2
CAR Sedan 180
BIKE Sports 160',
                    '[Vehicle Dispatch] CAR Sedan -> Max Speed: 180 km/h
[Vehicle Dispatch] BIKE Sports -> Max Speed: 160 km/h')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (10, '2
CAR Sedan 180
BIKE Sports 160', '[Vehicle Dispatch] CAR Sedan -> Max Speed: 180 km/h
[Vehicle Dispatch] BIKE Sports -> Max Speed: 160 km/h', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (10, '3
BIKE Cruiser 140
CAR SUV 200
CAR Hatchback 150', '[Vehicle Dispatch] BIKE Cruiser -> Max Speed: 140 km/h
[Vehicle Dispatch] CAR SUV -> Max Speed: 200 km/h
[Vehicle Dispatch] CAR Hatchback -> Max Speed: 150 km/h', false)
                """);
        

                // Problem 11: Unit 2: Abstract Classes & Abstract Methods
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (11, 'Unit 2: Abstract Classes & Abstract Methods',
                    'An Abstract class cannot be instantiated and may contain abstract methods that derived subclasses MUST implement. Design an abstract class ''BankAccount'' with abstract method ''double calculateAnnualInterest()''. Create subclasses:
- ''SAVINGS'': Interest rate is 4.0% per annum (balance * 0.04)
- ''CURRENT'': Interest rate is 1.5% per annum (balance * 0.015)
Read account type, customer name, and balance, then compute interest and final balance.',
                    'A single line: <type: SAVINGS/CURRENT> <holderName> <balance: double>',
                    'Account Holder: <name> | Type: <type> | Balance: <bal: 2 decimals> | Interest: <interest: 2 decimals> | New Balance: <newBal: 2 decimals>',
                    'balance >= 0',
                    'MEDIUM', 'UNIT_2_INHERITANCE', 150, 2000,
                    'SAVINGS JohnDoe 10000.00',
                    'Account Holder: JohnDoe | Type: SAVINGS | Balance: 10000.00 | Interest: 400.00 | New Balance: 10400.00')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (11, 'SAVINGS JohnDoe 10000.00', 'Account Holder: JohnDoe | Type: SAVINGS | Balance: 10000.00 | Interest: 400.00 | New Balance: 10400.00', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (11, 'CURRENT TechCorp 50000.00', 'Account Holder: TechCorp | Type: CURRENT | Balance: 50000.00 | Interest: 750.00 | New Balance: 50750.00', false)
                """);
        

                // Problem 12: Unit 2: Multiple Inheritance via Interfaces
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (12, 'Unit 2: Multiple Inheritance via Interfaces',
                    'While Java does not support multiple inheritance with classes, it achieves multiple inheritance of type through interfaces. Define two interfaces:
1. ''GPS'': method ''trackCoordinates(double lat, double lon)''
2. ''MediaPlayer'': method ''playTrack(String song)''
Implement both interfaces in a ''SmartDevice'' class.',
                    'A single line: <deviceModel> <latitude> <longitude> <songName>',
                    'Device: <deviceModel>
[GPS Interface] Tracking Coordinates: <lat>, <lon>
[MediaPlayer Interface] Playing Track: <songName>',
                    '-90 <= lat <= 90, -180 <= lon <= 180',
                    'MEDIUM', 'UNIT_2_INHERITANCE', 150, 2000,
                    'GalaxyPro 37.7749 -122.4194 BohemianRhapsody',
                    'Device: GalaxyPro
[GPS Interface] Tracking Coordinates: 37.7749, -122.4194
[MediaPlayer Interface] Playing Track: BohemianRhapsody')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (12, 'GalaxyPro 37.7749 -122.4194 BohemianRhapsody', 'Device: GalaxyPro
[GPS Interface] Tracking Coordinates: 37.7749, -122.4194
[MediaPlayer Interface] Playing Track: BohemianRhapsody', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (12, 'Pixel8 12.9716 77.5946 Imagine', 'Device: Pixel8
[GPS Interface] Tracking Coordinates: 12.9716, 77.5946
[MediaPlayer Interface] Playing Track: Imagine', false)
                """);
        

                // Problem 13: Unit 2: Collections Framework & Object Cloning
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (13, 'Unit 2: Collections Framework & Object Cloning',
                    'The Java Collections framework provides standard data structures like ArrayList and HashMap. Object Cloning using the ''Cloneable'' interface allows creating an exact duplicate of an object. Read N students with (id, name, score). Store them in a List, determine the top scorer, and demonstrate cloning a Student object by modifying the clone and verifying that original remains decoupled.',
                    'Line 1: N (number of students)
Next N lines: ID Name Score',
                    'Original Top Scorer: <Name> with score <Score>
Total Students Enrolled: <N>
Cloning Verification: SUCCESS (Objects are decoupled)',
                    '1 <= N <= 500',
                    'HARD', 'UNIT_2_INHERITANCE', 200, 2000,
                    '3
101 John 88
102 Emma 95
103 Dave 91',
                    'Original Top Scorer: Emma with score 95
Total Students Enrolled: 3
Cloning Verification: SUCCESS (Objects are decoupled)')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (13, '3
101 John 88
102 Emma 95
103 Dave 91', 'Original Top Scorer: Emma with score 95
Total Students Enrolled: 3
Cloning Verification: SUCCESS (Objects are decoupled)', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (13, '2
201 Alice 78
202 Bob 82', 'Original Top Scorer: Bob with score 82
Total Students Enrolled: 2
Cloning Verification: SUCCESS (Objects are decoupled)', false)
                """);
        

                // Problem 14: Unit 3: Built-in Exception Handling (try-catch-finally)
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (14, 'Unit 3: Built-in Exception Handling (try-catch-finally)',
                    'Exceptions in Java represent runtime errors. The try block contains code that might throw an exception, catch blocks handle specific exceptions, and the finally block ALWAYS executes regardless of outcome. Support two operations:
- ''DIV <a> <b>'': Evaluates a / b. Catches ArithmeticException if b == 0.
- ''PARSE <s>'': Parses integer. Catches NumberFormatException if not a valid integer.',
                    'Either ''DIV <a> <b>'' or ''PARSE <s>''',
                    'Result line followed by ''Cleanup: Finally block executed''',
                    'Operands fit in 32-bit signed integer',
                    'EASY', 'UNIT_3_EXCEPTIONS_THREADS', 100, 2000,
                    'DIV 10 0',
                    'Caught: ArithmeticException - Division by zero
Cleanup: Finally block executed')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (14, 'DIV 10 0', 'Caught: ArithmeticException - Division by zero
Cleanup: Finally block executed', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (14, 'DIV 20 4', 'Result: 5
Cleanup: Finally block executed', false)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (14, 'PARSE hello', 'Caught: NumberFormatException - Invalid numeric string
Cleanup: Finally block executed', false)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (14, 'PARSE 42', 'Result: 42
Cleanup: Finally block executed', false)
                """);
        

                // Problem 15: Unit 3: Custom User-Defined Exception (InvalidAgeException)
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (15, 'Unit 3: Custom User-Defined Exception (InvalidAgeException)',
                    'Java permits creating application-specific exceptions by subclassing Exception. Create a custom checked exception ''InvalidAgeException''. Write a method ''validateLicense(String name, int age) throws InvalidAgeException''. If age is less than 18 or greater than 120, throw InvalidAgeException; otherwise approve the candidate.',
                    'A single line: <candidateName> <age>',
                    'If valid: Candidate <name> (Age <age>) is eligible for driver license.
If invalid: Caught Custom Exception: InvalidAgeException - Age <age> is invalid for licensing.',
                    'Age is an integer',
                    'MEDIUM', 'UNIT_3_EXCEPTIONS_THREADS', 150, 2000,
                    'Rahul 16',
                    'Caught Custom Exception: InvalidAgeException - Age 16 is invalid for licensing.')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (15, 'Rahul 16', 'Caught Custom Exception: InvalidAgeException - Age 16 is invalid for licensing.', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (15, 'Ananya 21', 'Candidate Ananya (Age 21) is eligible for driver license.', false)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (15, 'OldMan 130', 'Caught Custom Exception: InvalidAgeException - Age 130 is invalid for licensing.', false)
                """);
        

                // Problem 16: Unit 3: Multiple Catch Blocks & Exception Hierarchy
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (16, 'Unit 3: Multiple Catch Blocks & Exception Hierarchy',
                    'When using multiple catch blocks, subclasses must be caught before their parent Exception class. Simulate handling different runtime faults based on a test code:
Code 1: Access array at out-of-bounds index -> Catch ArrayIndexOutOfBoundsException
Code 2: Call method on null reference -> Catch NullPointerException
Code 3: Successful array operation -> Compute and print sum.',
                    'A single integer testCode (1, 2, or 3). If 3, followed by N and N integers.',
                    'Corresponding handled exception message or success message.',
                    'testCode in {1, 2, 3}',
                    'MEDIUM', 'UNIT_3_EXCEPTIONS_THREADS', 150, 2000,
                    '1',
                    'Handled: ArrayIndexOutOfBoundsException')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (16, '1', 'Handled: ArrayIndexOutOfBoundsException', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (16, '2', 'Handled: NullPointerException', false)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (16, '3
4
10 20 30 40', 'Operation Successful: Sum = 100', false)
                """);
        

                // Problem 17: Unit 3: Thread Life Cycle & Runnable Interface
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (17, 'Unit 3: Thread Life Cycle & Runnable Interface',
                    'Multithreading in Java is implemented either by extending Thread or implementing the Runnable interface. Demonstrate the thread lifecycle: when created it is NEW, when started it enters RUNNABLE, executes its run() method sequentially for N steps, and finally joins entering TERMINATED state.',
                    'A thread name and iteration count N.',
                    'Sequential thread state trace:
Thread <name> State: NEW
Thread <name> State: RUNNABLE
[Task <name>] Step 1
...
Thread <name> State: TERMINATED',
                    '1 <= N <= 10',
                    'MEDIUM', 'UNIT_3_EXCEPTIONS_THREADS', 150, 2000,
                    'WorkerAlpha 3',
                    'Thread WorkerAlpha State: NEW
Thread WorkerAlpha State: RUNNABLE
[Task WorkerAlpha] Step 1
[Task WorkerAlpha] Step 2
[Task WorkerAlpha] Step 3
Thread WorkerAlpha State: TERMINATED')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (17, 'WorkerAlpha 3', 'Thread WorkerAlpha State: NEW
Thread WorkerAlpha State: RUNNABLE
[Task WorkerAlpha] Step 1
[Task WorkerAlpha] Step 2
[Task WorkerAlpha] Step 3
Thread WorkerAlpha State: TERMINATED', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (17, 'ComputeBot 2', 'Thread ComputeBot State: NEW
Thread ComputeBot State: RUNNABLE
[Task ComputeBot] Step 1
[Task ComputeBot] Step 2
Thread ComputeBot State: TERMINATED', false)
                """);
        

                // Problem 18: Unit 3: Thread Synchronization (Thread-Safe Counter)
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (18, 'Unit 3: Thread Synchronization (Thread-Safe Counter)',
                    'Thread Synchronization prevents data inconsistency caused by concurrent access to shared resources using the ''synchronized'' keyword. Simulate two concurrent worker threads operating on a shared BankAccount: Thread 1 deposits X amount across N transactions, Thread 2 withdraws Y amount across M transactions. Compute final synchronized balance.',
                    'Three space-separated integers: initialBalance, depositTotal, withdrawTotal',
                    'Initial Balance: <init> | Increments: <dep> | Decrements: <with> | Synchronized Final Balance: <final>',
                    'All balances are non-negative',
                    'HARD', 'UNIT_3_EXCEPTIONS_THREADS', 200, 2000,
                    '100 50 30',
                    'Initial Balance: 100 | Increments: 50 | Decrements: 30 | Synchronized Final Balance: 120')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (18, '100 50 30', 'Initial Balance: 100 | Increments: 50 | Decrements: 30 | Synchronized Final Balance: 120', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (18, '500 200 150', 'Initial Balance: 500 | Increments: 200 | Decrements: 150 | Synchronized Final Balance: 550', false)
                """);
        

                // Problem 19: Unit 3: Inter-Thread Communication (wait & notify)
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (19, 'Unit 3: Inter-Thread Communication (wait & notify)',
                    'Inter-thread communication allows synchronized threads to communicate using wait(), notify(), and notifyAll() defined in Object class. In a Producer-Consumer pattern, the Producer puts an item and notifies the Consumer, who waits until the item is available, retrieves it, and notifies the Producer.',
                    'Line 1: N (number of items)
Next N lines: Item tokens (Strings)',
                    'Alternating trace:
[PRODUCER] Put: <item>
[CONSUMER] Got: <item>
...
Exchange Complete: <N> items processed safely.',
                    '1 <= N <= 20',
                    'HARD', 'UNIT_3_EXCEPTIONS_THREADS', 200, 2000,
                    '2
MessageA
MessageB',
                    '[PRODUCER] Put: MessageA
[CONSUMER] Got: MessageA
[PRODUCER] Put: MessageB
[CONSUMER] Got: MessageB
Exchange Complete: 2 items processed safely.')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (19, '2
MessageA
MessageB', '[PRODUCER] Put: MessageA
[CONSUMER] Got: MessageA
[PRODUCER] Put: MessageB
[CONSUMER] Got: MessageB
Exchange Complete: 2 items processed safely.', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (19, '3
Alpha
Beta
Gamma', '[PRODUCER] Put: Alpha
[CONSUMER] Got: Alpha
[PRODUCER] Put: Beta
[CONSUMER] Got: Beta
[PRODUCER] Put: Gamma
[CONSUMER] Got: Gamma
Exchange Complete: 3 items processed safely.', false)
                """);
        

                // Problem 20: Unit 4: Console I/O & Structured Stream Parsing
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (20, 'Unit 4: Console I/O & Structured Stream Parsing',
                    'Console I/O in Java reads user keystrokes from standard input (System.in) and prints to standard output (System.out). Given N CSV records of student subjects in the format ''ID,Subject,Score'', parse each record using String.split or Scanner, and print a formatted table with aligned columns.',
                    'Line 1: N (number of records)
Next N lines: ID,Subject,Score',
                    'Table with header, divider, padded rows, and summary line:
ID       | SUBJECT    | SCORE
----------------------------
<padded rows>
Total Records Parsed: <N>',
                    '1 <= N <= 100',
                    'EASY', 'UNIT_4_IO_GENERICS_STRINGS', 100, 2000,
                    '2
101,Math,92
102,Java,98',
                    'ID       | SUBJECT    | SCORE
----------------------------
101      | Math       | 92
102      | Java       | 98
Total Records Parsed: 2')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (20, '2
101,Math,92
102,Java,98', 'ID       | SUBJECT    | SCORE
----------------------------
101      | Math       | 92
102      | Java       | 98
Total Records Parsed: 2', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (20, '3
201,Physics,85
202,Chem,78
203,CS,95', 'ID       | SUBJECT    | SCORE
----------------------------
201      | Physics    | 85
202      | Chem       | 78
203      | CS         | 95
Total Records Parsed: 3', false)
                """);
        

                // Problem 21: Unit 4: String Class & String Methods
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (21, 'Unit 4: String Class & String Methods',
                    'The String class in Java represents immutable sequences of characters. Given a single word or phrase, compute:
1. Length using length()
2. Uppercase conversion using toUpperCase()
3. Count of vowels (a, e, i, o, u case-insensitive)
4. Check if the string is a Palindrome (case-insensitive, ignoring non-alphanumeric chars).',
                    'A single line containing a word or string s.',
                    'Original: <s>
Length: <len>
Uppercase: <upper>
Vowels Count: <count>
Is Palindrome: <true/false>',
                    '1 <= s.length() <= 1000',
                    'EASY', 'UNIT_4_IO_GENERICS_STRINGS', 100, 2000,
                    'Racecar',
                    'Original: Racecar
Length: 7
Uppercase: RACECAR
Vowels Count: 3
Is Palindrome: true')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (21, 'Racecar', 'Original: Racecar
Length: 7
Uppercase: RACECAR
Vowels Count: 3
Is Palindrome: true', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (21, 'JavaProgramming', 'Original: JavaProgramming
Length: 15
Uppercase: JAVAPROGRAMMING
Vowels Count: 5
Is Palindrome: false', false)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (21, 'Madam', 'Original: Madam
Length: 5
Uppercase: MADAM
Vowels Count: 2
Is Palindrome: true', false)
                """);
        

                // Problem 22: Unit 4: StringBuffer & Mutable Strings
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (22, 'Unit 4: StringBuffer & Mutable Strings',
                    'Unlike immutable Strings, StringBuffer provides a thread-safe, mutable sequence of characters. Given an initial string, an append string, an insert offset, and an insert string, apply:
1. sb.append(appendText)
2. sb.insert(insertOffset, insertText)
3. sb.reverse()
Print the state after each transformation.',
                    'Four space-separated tokens: initialText appendText insertOffset insertText',
                    'Initial: <initial>
After Append: <appended>
After Insert: <inserted>
After Reverse: <reversed>
Final Length: <len>',
                    '0 <= insertOffset <= initialText.length() + appendText.length()',
                    'EASY', 'UNIT_4_IO_GENERICS_STRINGS', 100, 2000,
                    'Hello World 5 Beautiful',
                    'Initial: Hello
After Append: HelloWorld
After Insert: HelloBeautifulWorld
After Reverse: dlroWlufituaeBolleH
Final Length: 19')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (22, 'Hello World 5 Beautiful', 'Initial: Hello
After Append: HelloWorld
After Insert: HelloBeautifulWorld
After Reverse: dlroWlufituaeBolleH
Final Length: 19', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (22, 'Java 2026 4 Modern', 'Initial: Java
After Append: Java2026
After Insert: JavaModern2026
After Reverse: 6202nredoMavaJ
Final Length: 14', false)
                """);
        

                // Problem 23: Unit 4: Generics - Generic Classes & Methods
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (23, 'Unit 4: Generics - Generic Classes & Methods',
                    'Generics introduce type safety by allowing classes, interfaces, and methods to operate on parameterized types. Implement a generic class ''Container<T>'' that stores two elements of type T, provides a swap() method, and displays the elements.',
                    'Type tag (''INTEGER'' or ''STRING'') followed by two values.',
                    '[Generic Container<T>] Stored: Val1 = <v1>, Val2 = <v2> | Swapped: Val1 = <v2>, Val2 = <v1>',
                    'Values are valid for the given type tag',
                    'MEDIUM', 'UNIT_4_IO_GENERICS_STRINGS', 150, 2000,
                    'INTEGER 10 20',
                    '[Generic Container<T>] Stored: Val1 = 10, Val2 = 20 | Swapped: Val1 = 20, Val2 = 10')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (23, 'INTEGER 10 20', '[Generic Container<T>] Stored: Val1 = 10, Val2 = 20 | Swapped: Val1 = 20, Val2 = 10', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (23, 'STRING Alpha Beta', '[Generic Container<T>] Stored: Val1 = Alpha, Val2 = Beta | Swapped: Val1 = Beta, Val2 = Alpha', false)
                """);
        

                // Problem 24: Unit 4: Bounded Type Parameters (<T extends Number>)
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (24, 'Unit 4: Bounded Type Parameters (<T extends Number>)',
                    'Bounded Type Parameters restrict the types that can be used as type arguments in a parameterized class or method. Write a generic method ''<T extends Number> double calculateSum(List<T> list)'' that calculates the sum of numbers by calling doubleValue(). Calculate the total sum and arithmetic average formatted to 2 decimal places.',
                    'Line 1: N (count of numbers)
Line 2: N numbers (floating point or integer)',
                    'Count: <N> | Sum: <sum to 2 decimals> | Average: <avg to 2 decimals>',
                    '1 <= N <= 1000',
                    'MEDIUM', 'UNIT_4_IO_GENERICS_STRINGS', 150, 2000,
                    '4
10 20 35 45',
                    'Count: 4 | Sum: 110.00 | Average: 27.50')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (24, '4
10 20 35 45', 'Count: 4 | Sum: 110.00 | Average: 27.50', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (24, '3
1.5 2.5 3.0', 'Count: 3 | Sum: 7.00 | Average: 2.33', false)
                """);
        

                // Problem 25: Unit 5: Event Delegation Model (Action Event Simulator)
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (25, 'Unit 5: Event Delegation Model (Action Event Simulator)',
                    'The Java Event Delegation Model separates event sources from event handlers. An Event Source generates an Event Object (such as ActionEvent) and broadcasts it to registered Event Listeners (implementing ActionListener). Simulate this delegation pattern by processing a component ID and an action command.',
                    'Two space-separated strings: <buttonId> <actionCommand>',
                    'Three lines:
[Event Source] Button "<buttonId>" generated ActionEvent.
[Event Object] ActionCommand: "<actionCommand>", Source: <buttonId>
[Action Listener] actionPerformed() handled successfully.',
                    'Valid alphanumeric tokens',
                    'EASY', 'UNIT_5_GUI_JDBC', 100, 2000,
                    'btnSubmit SUBMIT_FORM',
                    '[Event Source] Button "btnSubmit" generated ActionEvent.
[Event Object] ActionCommand: "SUBMIT_FORM", Source: btnSubmit
[Action Listener] actionPerformed() handled successfully.')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (25, 'btnSubmit SUBMIT_FORM', '[Event Source] Button "btnSubmit" generated ActionEvent.
[Event Object] ActionCommand: "SUBMIT_FORM", Source: btnSubmit
[Action Listener] actionPerformed() handled successfully.', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (25, 'btnReset RESET_ALL', '[Event Source] Button "btnReset" generated ActionEvent.
[Event Object] ActionCommand: "RESET_ALL", Source: btnReset
[Action Listener] actionPerformed() handled successfully.', false)
                """);
        

                // Problem 26: Unit 5: Swing Layout Managers & Dimension Calculator
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (26, 'Unit 5: Swing Layout Managers & Dimension Calculator',
                    'Layout Managers in AWT and Swing (like FlowLayout, BorderLayout, GridLayout) manage the positioning and dimensions of GUI components inside a Container. Calculate the preferred container dimension for a GridLayout with specified rows, cols, cellWidth, cellHeight, and horizontal/vertical gaps.
Formula:
Total Width = cols * cellWidth + (cols - 1) * hGap
Total Height = rows * cellHeight + (rows - 1) * vGap',
                    'Six space-separated integers: rows cols cellWidth cellHeight hGap vGap',
                    'GridLayout Dimensions: <rows>x<cols> | Preferred Width: <width>px | Preferred Height: <height>px',
                    'rows, cols >= 1, dimensions >= 0',
                    'MEDIUM', 'UNIT_5_GUI_JDBC', 150, 2000,
                    '3 4 100 50 10 10',
                    'GridLayout Dimensions: 3x4 | Preferred Width: 430px | Preferred Height: 170px')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (26, '3 4 100 50 10 10', 'GridLayout Dimensions: 3x4 | Preferred Width: 430px | Preferred Height: 170px', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (26, '2 2 150 80 0 0', 'GridLayout Dimensions: 2x2 | Preferred Width: 300px | Preferred Height: 160px', false)
                """);
        

                // Problem 27: Unit 5: JDBC Architecture & PreparedStatement Query Builder
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (27, 'Unit 5: JDBC Architecture & PreparedStatement Query Builder',
                    'JDBC (Java Database Connectivity) architecture includes DriverManager, Connection, Statement, and PreparedStatement. A PreparedStatement prevents SQL injection by parameterizing inputs with ''?''. Given a table name, two column names, and two values, formulate the SQL statement and bound parameter mappings.',
                    'Five space-separated tokens: <tableName> <col1> <col2> <val1> <val2>',
                    'Four lines:
JDBC Driver: com.mysql.cj.jdbc.Driver loaded.
Connection URL: jdbc:mysql://localhost:3306/university_db
Prepared SQL: INSERT INTO <table> (<col1>, <col2>) VALUES (?, ?);
Bound Parameters: [1: <val1>, 2: <val2>]',
                    'Valid SQL identifiers',
                    'MEDIUM', 'UNIT_5_GUI_JDBC', 150, 2000,
                    'students student_name cgpa Alice 9.5',
                    'JDBC Driver: com.mysql.cj.jdbc.Driver loaded.
Connection URL: jdbc:mysql://localhost:3306/university_db
Prepared SQL: INSERT INTO students (student_name, cgpa) VALUES (?, ?);
Bound Parameters: [1: Alice, 2: 9.5]')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (27, 'students student_name cgpa Alice 9.5', 'JDBC Driver: com.mysql.cj.jdbc.Driver loaded.
Connection URL: jdbc:mysql://localhost:3306/university_db
Prepared SQL: INSERT INTO students (student_name, cgpa) VALUES (?, ?);
Bound Parameters: [1: Alice, 2: 9.5]', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (27, 'courses course_code credits CS201 4', 'JDBC Driver: com.mysql.cj.jdbc.Driver loaded.
Connection URL: jdbc:mysql://localhost:3306/university_db
Prepared SQL: INSERT INTO courses (course_code, credits) VALUES (?, ?);
Bound Parameters: [1: CS201, 2: 4]', false)
                """);
        

                // Problem 28: Unit 5: GUI + JDBC Student Record Formatter
                stmt.execute("""
                    INSERT INTO problems (problem_id, title, description, input_format, output_format, constraints, difficulty, category, points, time_limit, sample_input, sample_output)
                    VALUES (28, 'Unit 5: GUI + JDBC Student Record Formatter',
                    'In full-stack Java applications, database queries executed via JDBC ResultSet are rendered in Swing JTable or JavaFX TableView components. Given N student rows retrieved from a database query, format them into a structured ASCII table with aligned columns.',
                    'Line 1: N (number of records)
Next N lines: ID Name Course Marks',
                    '+-----+----------------+------------+-------+
| ID  | Name           | Course     | Marks |
+-----+----------------+------------+-------+
| <id padded 3> | <name padded 14> | <course padded 10> | <marks padded 5 right> |
...
+-----+----------------+------------+-------+
Total JDBC Records Displayed: <N>',
                    '1 <= N <= 100',
                    'HARD', 'UNIT_5_GUI_JDBC', 200, 2000,
                    '2
101 Alice CS201 95
102 Bob CS201 88',
                    '+-----+----------------+------------+-------+
| ID  | Name           | Course     | Marks |
+-----+----------------+------------+-------+
| 101 | Alice          | CS201      |    95 |
| 102 | Bob            | CS201      |    88 |
+-----+----------------+------------+-------+
Total JDBC Records Displayed: 2')
                """);
    

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (28, '2
101 Alice CS201 95
102 Bob CS201 88', '+-----+----------------+------------+-------+
| ID  | Name           | Course     | Marks |
+-----+----------------+------------+-------+
| 101 | Alice          | CS201      |    95 |
| 102 | Bob            | CS201      |    88 |
+-----+----------------+------------+-------+
Total JDBC Records Displayed: 2', true)
                """);
        

                stmt.execute("""
                    INSERT INTO test_cases (problem_id, input_data, expected_output, is_sample) VALUES
                    (28, '3
201 Charlie CS202 78
202 Diana CS202 91
203 Edward CS202 84', '+-----+----------------+------------+-------+
| ID  | Name           | Course     | Marks |
+-----+----------------+------------+-------+
| 201 | Charlie        | CS202      |    78 |
| 202 | Diana          | CS202      |    91 |
| 203 | Edward         | CS202      |    84 |
+-----+----------------+------------+-------+
Total JDBC Records Displayed: 3', false)
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
