package com.codingbattle.engine;

import com.codingbattle.model.Problem;
import com.codingbattle.model.Submission;

/**
 * Abstraction Interface for Solution Code Evaluation.
 * Demonstrates Abstraction in OOP.
 */
public interface CodeEvaluator {
    /**
     * Evaluates a submission against all test cases of the given problem.
     *
     * @param submission The student's code submission
     * @param problem    The problem definition with its test cases
     * @return Evaluated submission populated with test results, status, execution time, and score
     */
    Submission evaluate(Submission submission, Problem problem);
}
