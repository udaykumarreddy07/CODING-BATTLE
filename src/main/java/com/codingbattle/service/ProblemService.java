package com.codingbattle.service;

import com.codingbattle.dao.ProblemDAO;
import com.codingbattle.dao.TestCaseDAO;
import com.codingbattle.dsa.ProblemSearchIndex;
import com.codingbattle.model.Category;
import com.codingbattle.model.Difficulty;
import com.codingbattle.model.Problem;
import com.codingbattle.model.TestCase;

import java.util.List;
import java.util.Set;

/**
 * Service Layer for Problem management, search, and filtering using DSA indexes.
 */
public class ProblemService {

    private final ProblemDAO problemDAO;
    private final TestCaseDAO testCaseDAO;
    private ProblemSearchIndex searchIndex;

    public ProblemService() {
        this.problemDAO = new ProblemDAO();
        this.testCaseDAO = new TestCaseDAO();
        refreshSearchIndex();
    }

    public synchronized void refreshSearchIndex() {
        List<Problem> all = problemDAO.getAllProblems();
        this.searchIndex = new ProblemSearchIndex(all);
    }

    public List<Problem> getAllProblems() {
        return problemDAO.getAllProblems();
    }

    public List<Problem> getAllProblemsForStudent(int studentId) {
        List<Problem> list = problemDAO.getAllProblems();
        Set<Integer> solvedIds = problemDAO.getSolvedProblemIdsForUser(studentId);
        for (Problem p : list) {
            p.setSolvedByCurrentUser(solvedIds.contains(p.getProblemId()));
        }
        return list;
    }

    public Problem getProblemDetails(int problemId) {
        return problemDAO.getProblemById(problemId);
    }

    public Problem getProblemDetailsForStudent(int problemId, int studentId) {
        Problem p = problemDAO.getProblemById(problemId);
        if (p != null) {
            Set<Integer> solvedIds = problemDAO.getSolvedProblemIdsForUser(studentId);
            p.setSolvedByCurrentUser(solvedIds.contains(problemId));
        }
        return p;
    }

    public Set<Integer> getSolvedProblemIdsForUser(int studentId) {
        return problemDAO.getSolvedProblemIdsForUser(studentId);
    }

    public List<Problem> searchAndFilter(String query, Category category, Difficulty difficulty, Boolean solvedFilter, int studentId) {
        List<Problem> pool = getAllProblemsForStudent(studentId);
        ProblemSearchIndex index = new ProblemSearchIndex(pool);
        return index.filter(query, category, difficulty, solvedFilter);
    }

    public int createProblem(Problem problem) {
        int id = problemDAO.addProblem(problem);
        refreshSearchIndex();
        return id;
    }

    public boolean updateProblem(Problem problem) {
        boolean ok = problemDAO.updateProblem(problem);
        refreshSearchIndex();
        return ok;
    }

    public boolean deleteProblem(int problemId) {
        boolean ok = problemDAO.deleteProblem(problemId);
        refreshSearchIndex();
        return ok;
    }

    public List<TestCase> getTestCases(int problemId) {
        return testCaseDAO.getTestCasesByProblemId(problemId);
    }

    public boolean addTestCase(TestCase testCase) {
        return testCaseDAO.addTestCase(testCase);
    }

    public boolean deleteTestCase(int testId) {
        return testCaseDAO.deleteTestCase(testId);
    }
}
