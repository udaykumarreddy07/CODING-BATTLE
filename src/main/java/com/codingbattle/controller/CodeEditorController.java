package com.codingbattle.controller;

import com.codingbattle.dsa.EditorUndoStack;
import com.codingbattle.model.*;
import com.codingbattle.service.ProblemService;
import com.codingbattle.service.SubmissionService;
import com.codingbattle.util.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CodeEditorController {

    @FXML private Label problemTitleLabel;
    @FXML private Label difficultyBadge;
    @FXML private Label categoryBadge;
    @FXML private Label pointsBadge;
    @FXML private Label timeLimitBadge;

    @FXML private Label descriptionLabel;
    @FXML private Label inputFormatLabel;
    @FXML private Label outputFormatLabel;
    @FXML private Label constraintsLabel;
    @FXML private TextArea sampleInputArea;
    @FXML private TextArea sampleOutputArea;

    @FXML private TextArea codeArea;
    @FXML private Button runBtn;
    @FXML private Button submitBtn;
    @FXML private ProgressIndicator progressIndicator;

    @FXML private Label resultStatusBadge;
    @FXML private Label resultScoreBadge;
    @FXML private Label resultTimeBadge;
    @FXML private TextArea consoleOutputArea;
    @FXML private VBox testCasesContainer;

    @FXML private Button undoBtn;
    @FXML private Button redoBtn;

    private NavigationCallback navigationCallback;
    private final ProblemService problemService = new ProblemService();
    private final SubmissionService submissionService = new SubmissionService();

    private Problem currentProblem;
    private Integer activeBattleId;

    // DSA Stacks for Undo and Redo operations
    private final EditorUndoStack<String> undoStack = new EditorUndoStack<>();
    private final EditorUndoStack<String> redoStack = new EditorUndoStack<>();
    private boolean isUndoingOrRedoing = false;

    public void setNavigationCallback(NavigationCallback callback) {
        this.navigationCallback = callback;
    }

    @FXML
    public void initialize() {
        progressIndicator.setVisible(false);
        setupUndoRedoTracking();
    }

    private void setupUndoRedoTracking() {
        codeArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUndoingOrRedoing && oldVal != null && !oldVal.equals(newVal)) {
                undoStack.push(oldVal);
                redoStack.clear();
            }
        });
    }

    @FXML
    private void handleUndo(ActionEvent event) {
        if (!undoStack.isEmpty()) {
            isUndoingOrRedoing = true;
            redoStack.push(codeArea.getText());
            codeArea.setText(undoStack.pop());
            isUndoingOrRedoing = false;
        }
    }

    @FXML
    private void handleRedo(ActionEvent event) {
        if (!redoStack.isEmpty()) {
            isUndoingOrRedoing = true;
            undoStack.push(codeArea.getText());
            codeArea.setText(redoStack.pop());
            isUndoingOrRedoing = false;
        }
    }

    public void loadProblem(int problemId, Integer battleId) {
        this.activeBattleId = battleId;
        this.currentProblem = problemService.getProblemDetails(problemId);

        if (currentProblem != null) {
            problemTitleLabel.setText(currentProblem.getTitle());
            difficultyBadge.setText(currentProblem.getDifficulty().name());
            categoryBadge.setText(currentProblem.getCategory().getDisplayName());
            pointsBadge.setText(currentProblem.getPoints() + " pts");
            timeLimitBadge.setText(currentProblem.getTimeLimit() + " ms limit");

            descriptionLabel.setText(currentProblem.getDescription());
            inputFormatLabel.setText(currentProblem.getInputFormat() != null ? currentProblem.getInputFormat() : "Standard Input");
            outputFormatLabel.setText(currentProblem.getOutputFormat() != null ? currentProblem.getOutputFormat() : "Standard Output");
            constraintsLabel.setText(currentProblem.getConstraints() != null ? currentProblem.getConstraints() : "None");

            sampleInputArea.setText(currentProblem.getSampleInput());
            sampleOutputArea.setText(currentProblem.getSampleOutput());

            // Default boilerplate
            resetToDefaultTemplate();
        }
    }

    @FXML
    private void handleResetTemplate(ActionEvent event) {
        resetToDefaultTemplate();
    }

    private void resetToDefaultTemplate() {
        String template = """
            import java.util.*;

            public class Solution {
                public static void main(String[] args) {
                    Scanner sc = new Scanner(System.in);
                    // Write your solution here
                    
                }
            }
            """;
        codeArea.setText(template.stripIndent());
        undoStack.clear();
        redoStack.clear();
    }

    @FXML
    private void handleRunCode(ActionEvent event) {
        if (currentProblem == null) return;

        setEvaluatingState(true);
        String code = codeArea.getText();

        Task<Submission> task = new Task<>() {
            @Override
            protected Submission call() {
                return submissionService.runCode(currentProblem.getProblemId(), code);
            }
        };

        task.setOnSucceeded(e -> {
            setEvaluatingState(false);
            displayEvaluationResults(task.getValue(), false);
        });

        task.setOnFailed(e -> {
            setEvaluatingState(false);
            displayError("Evaluation failed: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    @FXML
    private void handleSubmitCode(ActionEvent event) {
        if (currentProblem == null) return;

        setEvaluatingState(true);
        String code = codeArea.getText();
        Student s = SessionManager.getStudent();
        int userId = s != null ? s.getUserId() : 1;

        Task<Submission> task = new Task<>() {
            @Override
            protected Submission call() {
                return submissionService.submitCode(userId, currentProblem.getProblemId(), activeBattleId, code);
            }
        };

        task.setOnSucceeded(e -> {
            setEvaluatingState(false);
            displayEvaluationResults(task.getValue(), true);
        });

        task.setOnFailed(e -> {
            setEvaluatingState(false);
            displayError("Submission failed: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private void setEvaluatingState(boolean running) {
        runBtn.setDisable(running);
        submitBtn.setDisable(running);
        progressIndicator.setVisible(running);
        if (running) {
            resultStatusBadge.setText("EVALUATING...");
            resultStatusBadge.setStyle("-fx-text-fill: #58a6ff;");
            testCasesContainer.getChildren().clear();
        }
    }

    private void displayEvaluationResults(Submission sub, boolean isSubmit) {
        resultStatusBadge.setText(sub.getStatus().getDisplayName());
        resultScoreBadge.setText("Score: " + sub.getScore() + " pts");
        resultTimeBadge.setText("Time: " + sub.getExecutionTimeMs() + " ms");

        if (sub.getStatus() == SubmissionStatus.ACCEPTED) {
            resultStatusBadge.setStyle("-fx-text-fill: #3fb950; -fx-font-weight: bold;");
        } else {
            resultStatusBadge.setStyle("-fx-text-fill: #f85149; -fx-font-weight: bold;");
        }

        // Compiler / System output
        if (sub.getCompilerOutput() != null && !sub.getCompilerOutput().isEmpty()) {
            consoleOutputArea.setText(sub.getCompilerOutput());
            consoleOutputArea.setVisible(true);
            consoleOutputArea.setManaged(true);
        } else {
            consoleOutputArea.setText("Compilation: OK\nExecution finished.");
            consoleOutputArea.setVisible(true);
            consoleOutputArea.setManaged(true);
        }

        // Render test case cards
        testCasesContainer.getChildren().clear();
        for (TestCaseResult res : sub.getTestCaseResults()) {
            VBox card = new VBox(4);
            card.getStyleClass().add(res.isPassed() ? "test-passed" : "test-failed");

            HBox header = new HBox(10);
            Label nameLabel = new Label("Test Case " + res.getTestIndex() + (res.isPassed() ? "  ✓ Passed" : "  ✗ Failed"));
            nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (res.isPassed() ? "#3fb950" : "#f85149"));

            Label timeLbl = new Label(res.getExecutionTimeMs() + " ms");
            timeLbl.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 11px;");

            header.getChildren().addAll(nameLabel, timeLbl);
            card.getChildren().add(header);

            if (!res.isPassed()) {
                if (res.getErrorMessage() != null && !res.getErrorMessage().isEmpty()) {
                    Label err = new Label("Reason: " + res.getErrorMessage());
                    err.setStyle("-fx-text-fill: #f85149; -fx-font-size: 11px;");
                    card.getChildren().add(err);
                }
                Label exp = new Label("Expected: " + res.getExpectedOutput());
                exp.setStyle("-fx-font-family: Consolas; -fx-text-fill: #c9d1d9; -fx-font-size: 11px;");
                Label act = new Label("Actual:   " + res.getActualOutput());
                act.setStyle("-fx-font-family: Consolas; -fx-text-fill: #f85149; -fx-font-size: 11px;");
                card.getChildren().addAll(exp, act);
            }

            testCasesContainer.getChildren().add(card);
        }
    }

    private void displayError(String msg) {
        resultStatusBadge.setText("ERROR");
        resultStatusBadge.setStyle("-fx-text-fill: #f85149;");
        consoleOutputArea.setText(msg);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        if (navigationCallback != null) {
            if (activeBattleId != null && activeBattleId > 0) {
                navigationCallback.navigateTo("BATTLES", activeBattleId);
            } else {
                navigationCallback.navigateTo("PRACTICE", null);
            }
        }
    }
}
