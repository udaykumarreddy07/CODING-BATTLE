package com.codingbattle.controller;

import com.codingbattle.model.Achievement;
import com.codingbattle.model.Student;
import com.codingbattle.service.AchievementService;
import com.codingbattle.service.SubmissionService;
import com.codingbattle.service.UserService;
import com.codingbattle.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class StudentPerformanceController {

    @FXML private Label totalAttemptedLabel;
    @FXML private Label totalSolvedLabel;
    @FXML private Label successRateLabel;
    @FXML private ProgressBar successProgressBar;

    @FXML private Label easyCountLabel;
    @FXML private Label mediumCountLabel;
    @FXML private Label hardCountLabel;

    @FXML private PieChart difficultyPieChart;
    @FXML private FlowPane badgesFlowPane;

    private final SubmissionService submissionService = new SubmissionService();
    private final AchievementService achievementService = new AchievementService();
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        loadStudentPerformance();
    }

    private void loadStudentPerformance() {
        Student current = SessionManager.getStudent();
        if (current == null) return;

        int userId = current.getUserId();
        Map<String, Integer> stats = submissionService.getUserStats(userId);

        int attempted = stats.getOrDefault("TOTAL_ATTEMPTED", 0);
        int solved = stats.getOrDefault("TOTAL_SOLVED", 0);
        int easy = stats.getOrDefault("EASY_SOLVED", 0);
        int medium = stats.getOrDefault("MEDIUM_SOLVED", 0);
        int hard = stats.getOrDefault("HARD_SOLVED", 0);

        totalAttemptedLabel.setText(String.valueOf(attempted));
        totalSolvedLabel.setText(String.valueOf(solved));

        double rate = (attempted > 0) ? ((double) solved / attempted) * 100.0 : 0.0;
        successRateLabel.setText(String.format("%.1f%%", rate));
        successProgressBar.setProgress(attempted > 0 ? (double) solved / attempted : 0.0);

        easyCountLabel.setText(easy + " solved");
        mediumCountLabel.setText(medium + " solved");
        hardCountLabel.setText(hard + " solved");

        // Populate PieChart
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        if (easy > 0) pieData.add(new PieChart.Data("Easy (" + easy + ")", easy));
        if (medium > 0) pieData.add(new PieChart.Data("Medium (" + medium + ")", medium));
        if (hard > 0) pieData.add(new PieChart.Data("Hard (" + hard + ")", hard));
        if (pieData.isEmpty()) {
            pieData.add(new PieChart.Data("No Solved Problems Yet", 1));
        }
        difficultyPieChart.setData(pieData);

        // Load Badges
        List<Achievement> achievements = achievementService.getStudentAchievements(userId);
        badgesFlowPane.getChildren().clear();

        for (Achievement ach : achievements) {
            VBox badgeCard = new VBox(6);
            badgeCard.setAlignment(Pos.CENTER);
            badgeCard.setPrefWidth(190);
            badgeCard.getStyleClass().add("card");

            String iconChar = switch (ach.getBadgeCode()) {
                case "FIRST_BLOOD" -> "🏅";
                case "STREAK_5" -> "🔥";
                case "SPEED_DEMON" -> "⚡";
                case "TEN_PROBLEMS" -> "💯";
                case "BATTLE_WINNER" -> "🏆";
                case "TOP_TEN" -> "👑";
                case "FIFTY_PROBLEMS" -> "🚀";
                default -> "🎖️";
            };

            Label iconLbl = new Label(iconChar);
            iconLbl.setStyle("-fx-font-size: 28px;");

            Label nameLbl = new Label(ach.getName());
            nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

            Label descLbl = new Label(ach.getDescription());
            descLbl.setWrapText(true);
            descLbl.setAlignment(Pos.CENTER);
            descLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b949e; -fx-text-alignment: center;");

            Label statusLbl = new Label();
            if (ach.isUnlocked()) {
                badgeCard.setStyle("-fx-border-color: #2ea043; -fx-background-color: rgba(35, 134, 54, 0.1);");
                statusLbl.setText("UNLOCKED ✓");
                statusLbl.getStyleClass().addAll("badge-pill", "badge-easy");
            } else {
                badgeCard.setStyle("-fx-opacity: 0.55;");
                statusLbl.setText("LOCKED 🔒");
                statusLbl.getStyleClass().addAll("badge-pill");
                statusLbl.setStyle("-fx-text-fill: #8b949e; -fx-border-color: #30363d;");
            }

            badgeCard.getChildren().addAll(iconLbl, nameLbl, descLbl, statusLbl);
            badgesFlowPane.getChildren().add(badgeCard);
        }
    }
}
