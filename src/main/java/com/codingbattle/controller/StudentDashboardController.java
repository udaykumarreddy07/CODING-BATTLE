package com.codingbattle.controller;

import com.codingbattle.model.Battle;
import com.codingbattle.model.BattleStatus;
import com.codingbattle.model.Student;
import com.codingbattle.model.Submission;
import com.codingbattle.service.BattleService;
import com.codingbattle.service.SubmissionService;
import com.codingbattle.service.UserService;
import com.codingbattle.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class StudentDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label statSolvedLabel;
    @FXML private Label statRankLabel;
    @FXML private Label statPointsLabel;
    @FXML private Label statBattlesLabel;

    @FXML private HBox liveBattleBanner;
    @FXML private Label liveBattleTitleLabel;
    @FXML private Button joinLiveBattleBtn;

    @FXML private TableView<Submission> recentSubmissionsTable;
    @FXML private TableColumn<Submission, String> colProblem;
    @FXML private TableColumn<Submission, String> colStatus;
    @FXML private TableColumn<Submission, String> colScore;
    @FXML private TableColumn<Submission, String> colTime;
    @FXML private TableColumn<Submission, String> colDate;

    private NavigationCallback navigationCallback;
    private final UserService userService = new UserService();
    private final BattleService battleService = new BattleService();
    private final SubmissionService submissionService = new SubmissionService();
    private Integer liveBattleId = null;

    public void setNavigationCallback(NavigationCallback callback) {
        this.navigationCallback = callback;
    }

    @FXML
    public void initialize() {
        setupTable();
        loadDashboardData();
    }

    private void setupTable() {
        colProblem.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProblemTitle() != null ? data.getValue().getProblemTitle() : "Problem #" + data.getValue().getProblemId()));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().getDisplayName()));
        colScore.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getScore() + " pts"));
        colTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getExecutionTimeMs() + " ms"));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getSubmittedAt() != null ? data.getValue().getSubmittedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : ""
        ));
    }

    private void loadDashboardData() {
        Student current = SessionManager.getStudent();
        if (current != null) {
            Student refreshed = userService.getStudentProfile(current.getUserId());
            if (refreshed != null) {
                welcomeLabel.setText("Welcome back, " + refreshed.getUsername() + "!");
                statSolvedLabel.setText(String.valueOf(refreshed.getSolvedProblems()));
                statPointsLabel.setText(String.valueOf(refreshed.getTotalPoints()));
                statBattlesLabel.setText(String.valueOf(refreshed.getBattlesParticipated()));
                statRankLabel.setText(refreshed.getCurrentRank() > 0 ? "#" + refreshed.getCurrentRank() : "#1");
            }

            // Load recent submissions
            List<Submission> submissions = submissionService.getUserSubmissions(current.getUserId());
            recentSubmissionsTable.setItems(FXCollections.observableArrayList(submissions));
        }

        // Check for live battles
        List<Battle> battles = battleService.getAllBattles();
        Battle activeBattle = battles.stream().filter(b -> b.getStatus() == BattleStatus.LIVE).findFirst().orElse(null);
        if (activeBattle != null) {
            liveBattleBanner.setVisible(true);
            liveBattleBanner.setManaged(true);
            liveBattleId = activeBattle.getBattleId();
            liveBattleTitleLabel.setText("⚔️ " + activeBattle.getBattleName() + " (" + activeBattle.getDurationMinutes() + " mins)");
        } else {
            liveBattleBanner.setVisible(false);
            liveBattleBanner.setManaged(false);
        }
    }

    @FXML
    private void handleJoinLiveBattle(ActionEvent event) {
        if (navigationCallback != null && liveBattleId != null) {
            navigationCallback.navigateTo("BATTLES", liveBattleId);
        }
    }

    @FXML
    private void handleQuickPractice(ActionEvent event) {
        if (navigationCallback != null) {
            navigationCallback.navigateTo("PRACTICE", null);
        }
    }

    @FXML
    private void handleQuickLeaderboard(ActionEvent event) {
        if (navigationCallback != null) {
            navigationCallback.navigateTo("LEADERBOARD", null);
        }
    }

    @FXML
    private void handleQuickPerformance(ActionEvent event) {
        if (navigationCallback != null) {
            navigationCallback.navigateTo("PERFORMANCE", null);
        }
    }
}
