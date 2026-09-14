package com.codingbattle.engine;

import com.codingbattle.model.*;

import javax.tools.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Sandboxed Java Code Evaluator.
 * Safely compiles and executes student Java solutions against test cases.
 * Features:
 * - Forbidden API Security Scanner
 * - Isolated temporary build workspaces
 * - Native compiler or javac fallback
 * - Strict per-test execution timeout watchdog
 * - Memory & output truncation safeguards
 */
public class JavaCodeEvaluator implements CodeEvaluator {

    private static final int DEFAULT_TIMEOUT_MS = 2500;
    private static final int MAX_OUTPUT_CHARS = 50000;
    private final ScoreCalculator scoreCalculator;

    // Security pattern to catch hazardous calls in student code
    private static final Pattern FORBIDDEN_PATTERNS = Pattern.compile(
            "\\b(Runtime\\.getRuntime|ProcessBuilder|System\\.exit|sun\\.misc|java\\.lang\\.reflect|NativeMethodAccessorImpl)\\b"
    );

    public JavaCodeEvaluator() {
        this.scoreCalculator = new DefaultScoreCalculator();
    }

    public JavaCodeEvaluator(ScoreCalculator scoreCalculator) {
        this.scoreCalculator = scoreCalculator != null ? scoreCalculator : new DefaultScoreCalculator();
    }

    @Override
    public Submission evaluate(Submission submission, Problem problem) {
        String sourceCode = submission.getSourceCode();
        if (sourceCode == null || sourceCode.trim().isEmpty()) {
            submission.setStatus(SubmissionStatus.COMPILATION_ERROR);
            submission.setCompilerOutput("Source code is empty.");
            submission.setScore(0);
            return submission;
        }

        // Security check
        if (FORBIDDEN_PATTERNS.matcher(sourceCode).find()) {
            submission.setStatus(SubmissionStatus.SECURITY_VIOLATION);
            submission.setCompilerOutput("Security Violation: Access to system runtime, process execution, or reflection is forbidden.");
            submission.setScore(0);
            return submission;
        }

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("cb_eval_");
            Path sourceFile = tempDir.resolve("Solution.java");

            // Normalize public class name if student wrote 'public class Main' or 'public class Solution'
            String normalizedCode = normalizeClassDeclaration(sourceCode);
            Files.writeString(sourceFile, normalizedCode, StandardCharsets.UTF_8);

            // Compile the solution
            String compileError = compileJavaFile(sourceFile, tempDir);
            if (compileError != null) {
                submission.setStatus(SubmissionStatus.COMPILATION_ERROR);
                submission.setCompilerOutput(compileError);
                submission.setScore(0);
                return submission;
            }

            // Compilation succeeded: evaluate against test cases
            List<TestCase> testCases = problem.getTestCases();
            if (testCases.isEmpty()) {
                // If problem has no explicit DB test cases, construct one from sample
                testCases = new ArrayList<>();
                testCases.add(new TestCase(problem.getProblemId(), problem.getSampleInput(), problem.getSampleOutput(), true));
            }

            int passedCount = 0;
            long totalExecutionTime = 0;
            boolean hasTle = false;
            boolean hasRuntimeError = false;
            List<TestCaseResult> results = new ArrayList<>();

            int timeLimit = problem.getTimeLimit() > 0 ? problem.getTimeLimit() : DEFAULT_TIMEOUT_MS;

            for (int i = 0; i < testCases.size(); i++) {
                TestCase tc = testCases.get(i);
                TestCaseResult res = executeTestCase(tempDir, tc.getInputData(), tc.getExpectedOutput(), timeLimit, i + 1);
                results.add(res);
                totalExecutionTime += res.getExecutionTimeMs();

                if (res.isPassed()) {
                    passedCount++;
                } else {
                    if ("Time Limit Exceeded".equalsIgnoreCase(res.getErrorMessage())) {
                        hasTle = true;
                    } else if (res.getErrorMessage() != null && !res.getErrorMessage().isEmpty()
                            && !"Wrong Answer".equalsIgnoreCase(res.getErrorMessage())) {
                        hasRuntimeError = true;
                    }
                }
            }

            submission.setTestCaseResults(results);
            submission.setPassedTestCases(passedCount);
            submission.setTotalTestCases(testCases.size());
            submission.setExecutionTimeMs(totalExecutionTime);

            // Determine final status
            if (passedCount == testCases.size()) {
                submission.setStatus(SubmissionStatus.ACCEPTED);
            } else if (hasTle) {
                submission.setStatus(SubmissionStatus.TIME_LIMIT_EXCEEDED);
            } else if (hasRuntimeError) {
                submission.setStatus(SubmissionStatus.RUNTIME_ERROR);
            } else {
                submission.setStatus(SubmissionStatus.WRONG_ANSWER);
            }

            // Calculate score
            int finalScore = scoreCalculator.calculateScore(
                    problem.getDifficulty(),
                    problem.getPoints(),
                    passedCount,
                    testCases.size(),
                    totalExecutionTime
            );
            submission.setScore(finalScore);

        } catch (Exception e) {
            submission.setStatus(SubmissionStatus.RUNTIME_ERROR);
            submission.setCompilerOutput("Internal Evaluation Error: " + e.getMessage());
            submission.setScore(0);
        } finally {
            if (tempDir != null) {
                cleanUpDirectory(tempDir.toFile());
            }
        }

        return submission;
    }

    private String normalizeClassDeclaration(String sourceCode) {
        // Replace 'public class <anything>' with 'public class Solution'
        return sourceCode.replaceAll("public\\s+class\\s+[A-Za-z0-9_]+", "public class Solution");
    }

    private String compileJavaFile(Path sourceFile, Path outputDir) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler != null) {
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8);
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(List.of(sourceFile.toFile()));

            List<String> options = List.of("-d", outputDir.toString());
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
            boolean success = task.call();

            try {
                fileManager.close();
            } catch (IOException ignored) {}

            if (!success) {
                StringBuilder sb = new StringBuilder();
                for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
                    sb.append("Line ").append(d.getLineNumber())
                            .append(": ").append(d.getMessage(Locale.ENGLISH))
                            .append("\n");
                }
                return sb.toString().trim();
            }
            return null; // No compile errors
        }

        // Fallback to command-line javac
        try {
            ProcessBuilder pb = new ProcessBuilder("javac", "-d", outputDir.toString(), sourceFile.toString());
            pb.redirectErrorStream(true);
            Process p = pb.start();
            String output = readStream(p.getInputStream(), 10000);
            int exitCode = p.waitFor();
            if (exitCode != 0) {
                return output.isEmpty() ? "Compilation failed with exit code " + exitCode : output;
            }
            return null;
        } catch (Exception e) {
            return "Unable to invoke Java compiler: " + e.getMessage();
        }
    }

    private TestCaseResult executeTestCase(Path classDir, String inputData, String expectedOutput, int timeoutMs, int testIndex) {
        long startTime = System.currentTimeMillis();
        Process process = null;

        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-cp", classDir.toString(), "Solution");
            process = pb.start();

            // Write input to standard input
            if (inputData != null && !inputData.isEmpty()) {
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
                    writer.write(inputData);
                    writer.flush();
                }
            } else {
                process.getOutputStream().close();
            }

            // Await execution with timeout
            boolean completed = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            long elapsed = System.currentTimeMillis() - startTime;

            if (!completed) {
                process.destroyForcibly();
                return new TestCaseResult(testIndex, false, inputData, expectedOutput, "", elapsed, "Time Limit Exceeded");
            }

            int exitCode = process.exitValue();
            String stdout = readStream(process.getInputStream(), MAX_OUTPUT_CHARS);
            String stderr = readStream(process.getErrorStream(), MAX_OUTPUT_CHARS);

            if (exitCode != 0) {
                String errorMsg = stderr.isEmpty() ? "Exited with code " + exitCode : stderr;
                return new TestCaseResult(testIndex, false, inputData, expectedOutput, stdout, elapsed, "Runtime Error: " + errorMsg);
            }

            // Normalize and compare
            String normalizedActual = normalizeOutput(stdout);
            String normalizedExpected = normalizeOutput(expectedOutput);

            boolean passed = normalizedActual.equals(normalizedExpected);
            String errMsg = passed ? "" : "Wrong Answer";

            return new TestCaseResult(testIndex, passed, inputData, expectedOutput, stdout, elapsed, errMsg);

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            if (process != null) {
                process.destroyForcibly();
            }
            return new TestCaseResult(testIndex, false, inputData, expectedOutput, "", elapsed, e.getMessage());
        }
    }

    private String readStream(InputStream is, int maxChars) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            char[] buf = new char[1024];
            int read;
            while ((read = reader.read(buf)) != -1) {
                sb.append(buf, 0, read);
                if (sb.length() >= maxChars) {
                    sb.append("\n[Output truncated]");
                    break;
                }
            }
        }
        return sb.toString();
    }

    private String normalizeOutput(String raw) {
        if (raw == null) return "";
        // Normalize CRLF to LF, trim surrounding whitespace, split by lines, trim each line
        String[] lines = raw.replace("\r\n", "\n").replace("\r", "\n").trim().split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            sb.append(lines[i].trim());
            if (i < lines.length - 1) {
                sb.append("\n");
            }
        }
        return sb.toString().trim();
    }

    private void cleanUpDirectory(File dir) {
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    cleanUpDirectory(f);
                } else {
                    f.delete();
                }
            }
        }
        dir.delete();
    }
}
