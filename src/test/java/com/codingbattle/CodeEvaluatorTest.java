package com.codingbattle;

import com.codingbattle.database.DatabaseInitializer;
import com.codingbattle.engine.JavaCodeEvaluator;
import com.codingbattle.model.Problem;
import com.codingbattle.model.Submission;
import com.codingbattle.model.SubmissionStatus;
import com.codingbattle.service.ProblemService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CodeEvaluatorTest {

    private static JavaCodeEvaluator evaluator;
    private static ProblemService problemService;

    @BeforeAll
    public static void setup() {
        DatabaseInitializer.initialize();
        evaluator = new JavaCodeEvaluator();
        problemService = new ProblemService();
    }

    @Test
    public void testCorrectSolutionPalindrome() {
        Problem problem = problemService.getProblemDetails(2); // Palindrome Number
        assertNotNull(problem);

        String correctCode = """
            import java.util.Scanner;

            public class Solution {
                public static void main(String[] args) {
                    Scanner sc = new Scanner(System.in);
                    if (!sc.hasNextInt()) return;
                    int x = sc.nextInt();
                    if (x < 0) {
                        System.out.println("false");
                        return;
                    }
                    int orig = x;
                    long rev = 0;
                    while (x > 0) {
                        rev = rev * 10 + (x % 10);
                        x /= 10;
                    }
                    System.out.println(rev == orig ? "true" : "false");
                }
            }
        """;

        Submission sub = new Submission();
        sub.setSourceCode(correctCode);
        sub.setProblemId(problem.getProblemId());

        Submission result = evaluator.evaluate(sub, problem);
        assertEquals(SubmissionStatus.ACCEPTED, result.getStatus());
        assertTrue(result.getScore() > 0, "Accepted submission should earn positive score");
        assertEquals(result.getTotalTestCases(), result.getPassedTestCases());
    }

    @Test
    public void testCompilationError() {
        Problem problem = problemService.getProblemDetails(2);
        assertNotNull(problem);

        String badCode = """
            public class Solution {
                public static void main(String[] args) {
                    ThisIsNotValidJavaCode syntax error;
                }
            }
        """;

        Submission sub = new Submission();
        sub.setSourceCode(badCode);

        Submission result = evaluator.evaluate(sub, problem);
        assertEquals(SubmissionStatus.COMPILATION_ERROR, result.getStatus());
        assertEquals(0, result.getScore());
        assertNotNull(result.getCompilerOutput());
    }

    @Test
    public void testSecurityViolation() {
        Problem problem = problemService.getProblemDetails(2);
        assertNotNull(problem);

        String maliciousCode = """
            public class Solution {
                public static void main(String[] args) {
                    Runtime.getRuntime().exec("calc.exe");
                }
            }
        """;

        Submission sub = new Submission();
        sub.setSourceCode(maliciousCode);

        Submission result = evaluator.evaluate(sub, problem);
        assertEquals(SubmissionStatus.SECURITY_VIOLATION, result.getStatus());
        assertEquals(0, result.getScore());
    }

    @Test
    public void testInfiniteLoopTimeout() {
        Problem problem = problemService.getProblemDetails(2);
        assertNotNull(problem);

        String infiniteLoopCode = """
            public class Solution {
                public static void main(String[] args) {
                    while (true) {
                        // Loop forever
                    }
                }
            }
        """;

        Submission sub = new Submission();
        sub.setSourceCode(infiniteLoopCode);

        Submission result = evaluator.evaluate(sub, problem);
        assertEquals(SubmissionStatus.TIME_LIMIT_EXCEEDED, result.getStatus());
        assertEquals(0, result.getScore());
    }
}
