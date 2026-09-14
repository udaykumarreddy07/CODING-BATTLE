package com.codingbattle.service;

import com.codingbattle.dao.ProblemDAO;
import com.codingbattle.dao.ScoreDAO;
import com.codingbattle.dao.SubmissionDAO;
import com.codingbattle.engine.CodeEvaluator;
import com.codingbattle.engine.JavaCodeEvaluator;
import com.codingbattle.model.Problem;
import com.codingbattle.model.Submission;
import com.codingbattle.model.SubmissionStatus;

import java.util.List;
import java.util.Map;

/**
 * Service Layer for Solution Evaluation and Submission orchestration.
 */
public class SubmissionService {

    private final SubmissionDAO submissionDAO;
    private final ProblemDAO problemDAO;
    private final ScoreDAO scoreDAO;
    private final CodeEvaluator codeEvaluator;
    private final AchievementService achievementService;

    public SubmissionService() {
        this.submissionDAO = new SubmissionDAO();
        this.problemDAO = new ProblemDAO();
        this.scoreDAO = new ScoreDAO();
        this.codeEvaluator = new JavaCodeEvaluator();
        this.achievementService = new AchievementService();
    }

    /**
     * Dry-run for student testing ("Run Code"):
     * Evaluates code against sample/all test cases without saving to DB.
     */
    public Submission runCode(int problemId, String sourceCode) {
        Problem problem = problemDAO.getProblemById(problemId);
        Submission sub = new Submission();
        sub.setProblemId(problemId);
        sub.setSourceCode(sourceCode);
        return codeEvaluator.evaluate(sub, problem);
    }

    /**
     * Formal submission ("Submit Code"):
     * Evaluates against all test cases, persists submission, updates scores, and awards badges.
     */
    public Submission submitCode(int userId, int problemId, Integer battleId, String sourceCode) {
        Problem problem = problemDAO.getProblemById(problemId);

        Submission sub = new Submission();
        sub.setUserId(userId);
        sub.setProblemId(problemId);
        sub.setBattleId(battleId);
        sub.setSourceCode(sourceCode);

        // Evaluate
        Submission evaluated = codeEvaluator.evaluate(sub, problem);

        // Persist
        submissionDAO.saveSubmission(evaluated);

        // If in battle and accepted, update battle score
        if (evaluated.getStatus() == SubmissionStatus.ACCEPTED && battleId != null && battleId > 0) {
            scoreDAO.upsertBattleScore(userId, battleId, evaluated.getScore());
        }

        // Trigger Gamification Checks
        achievementService.checkAndAwardBadges(userId, evaluated);

        return evaluated;
    }

    public List<Submission> getUserSubmissions(int userId) {
        return submissionDAO.getSubmissionsByUserId(userId);
    }

    public List<Submission> getAllSubmissions() {
        return submissionDAO.getAllSubmissions();
    }

    public Map<String, Integer> getUserStats(int userId) {
        return submissionDAO.getUserPerformanceStats(userId);
    }
}
