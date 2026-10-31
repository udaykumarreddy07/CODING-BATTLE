package com.codingbattle.service;

import com.codingbattle.model.Assignment;
import com.codingbattle.model.AssignmentSubmission;
import com.codingbattle.model.Category;
import com.codingbattle.model.Difficulty;
import com.codingbattle.model.Problem;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service managing academic assignments, coursework exams, and grading telemetry.
 */
public class AssignmentService {

    private final Map<Integer, Assignment> assignmentStore = new ConcurrentHashMap<>();
    private final Map<Integer, List<AssignmentSubmission>> submissionStore = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(100);

    public AssignmentService() {
        seedInitialAssignments();
    }

    private void seedInitialAssignments() {
        // Sample Problem for assignment
        Problem p1 = new Problem(1, "Two Sum II", "Find two numbers in array that add to target",
                Category.ARRAYS, Difficulty.EASY, 100);
        Problem p2 = new Problem(2, "Valid Parentheses Stack", "Determine if brackets are valid",
                Category.STACK, Difficulty.MEDIUM, 100);

        Assignment a1 = new Assignment(1, "Midterm Exam 1: Arrays & Memory Models",
                "Official timed examination covering static arrays, memory allocation, and algorithmic complexity.",
                "Data Structures", LocalDateTime.now().plusDays(2), 60, 200, true, 42);
        a1.setProblems(Arrays.asList(p1, p2));
        a1.setSubmissionsCount(28);
        assignmentStore.put(1, a1);

        Assignment a2 = new Assignment(2, "Weekly Lab 4: LinkedLists & Pointer Manipulations",
                "Practical laboratory exercises implementing singly and doubly linked lists with cycle detection.",
                "Algorithms", LocalDateTime.now().plusDays(5), 90, 150, false, 42);
        a2.setProblems(Collections.singletonList(p1));
        a2.setSubmissionsCount(35);
        assignmentStore.put(2, a2);

        Assignment a3 = new Assignment(3, "Binary Trees & BST In-Order Traversal Challenge",
                "Recursive and iterative tree traversal techniques and balancing invariant verifications.",
                "Trees & Graphs", LocalDateTime.now().plusDays(8), 75, 100, true, 42);
        a3.setProblems(Collections.singletonList(p2));
        a3.setSubmissionsCount(19);
        assignmentStore.put(3, a3);

        // Seed sample roster submissions
        List<AssignmentSubmission> subs1 = new ArrayList<>();
        subs1.add(new AssignmentSubmission(101, 1, 1, "Rahul Sharma", "rahul@student.edu",
                195, 200, 10, 10,
                "public class Solution {\n    public int[] twoSum(int[] numbers, int target) {\n        int l = 0, r = numbers.length - 1;\n        while (l < r) {\n            int sum = numbers[l] + numbers[r];\n            if (sum == target) return new int[]{l + 1, r + 1};\n            if (sum < target) l++; else r--;\n        }\n        return new int[0];\n    }\n}",
                "ALL PASS", 0));
        subs1.add(new AssignmentSubmission(102, 1, 2, "Priya Patel", "priya@student.edu",
                180, 200, 9, 10,
                "// Two pointers implementation\nclass Solution { ... }",
                "PARTIAL PASS", 0));
        subs1.add(new AssignmentSubmission(103, 1, 3, "Amit Verma", "amit@student.edu",
                140, 200, 7, 10,
                "// Brute force solution with hashmap",
                "TIME LIMIT EXCEEDED", 1));
        submissionStore.put(1, subs1);
    }

    public List<Assignment> getAllAssignments() {
        return new ArrayList<>(assignmentStore.values());
    }

    public Optional<Assignment> getAssignmentById(int id) {
        return Optional.ofNullable(assignmentStore.get(id));
    }

    public Assignment createAssignment(String title, String description, String category,
                                      LocalDateTime deadline, int duration, int totalMarks,
                                      boolean proctoring, List<Problem> problems) {
        int newId = idGenerator.incrementAndGet();
        Assignment assignment = new Assignment(newId, title, description, category, deadline,
                duration, totalMarks, proctoring, 42);
        if (problems != null) {
            assignment.setProblems(problems);
        }
        assignmentStore.put(newId, assignment);
        submissionStore.put(newId, new ArrayList<>());
        return assignment;
    }

    public List<AssignmentSubmission> getSubmissionsForAssignment(int assignmentId) {
        return submissionStore.getOrDefault(assignmentId, new ArrayList<>());
    }

    public boolean adjustScore(int assignmentId, int submissionId, int newScore) {
        List<AssignmentSubmission> subs = submissionStore.get(assignmentId);
        if (subs == null) return false;
        for (AssignmentSubmission sub : subs) {
            if (sub.getSubmissionId() == submissionId) {
                sub.setScore(newScore);
                return true;
            }
        }
        return false;
    }

    public boolean extendDeadline(int assignmentId, int extraHours) {
        Assignment a = assignmentStore.get(assignmentId);
        if (a != null && a.getDeadline() != null) {
            a.setDeadline(a.getDeadline().plusHours(extraHours));
            return true;
        }
        return false;
    }
}
