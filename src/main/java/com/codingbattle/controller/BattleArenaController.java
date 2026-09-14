package com.codingbattle.controller;

import com.codingbattle.model.*;
import com.codingbattle.service.BattleService;
import com.codingbattle.service.ProblemService;
import com.codingbattle.util.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.Set;

public class BattleArenaController {

    @FXML private ComboBox<Battle> battleSelectCombo;
    @FXML private Label battleTitleLabel;
    @FXML private Label battleStatusBadge;
    @FXML private Label timerLabel;
    @FXML private Label solvedCountLabel;

    @FXML private HBox problemsRibbon;

    @FXML private VBox activeBattlePanel;
    @FXML private TableView<Score> battleScoreTable;
    @FXML private TableColumn<Score, String> colRank;
    @FXML private TableColumn<Score, String> colUser;
    @FXML private TableColumn<Score, String> colScore;
    @FXML private TableColumn<Score, String> colSolved;

    private NavigationCallback navigationCallback;
    private final BattleService battleService = new BattleService();
    private final ProblemService problemService = new ProblemService();

    private Battle activeBattle;
    private Timeline countdownTimeline;
    private int remainingSeconds = 0;

    public void setNavigationCallback(NavigationCallback callback) {
        this.navigationCallback = callback;
    }

    @FXML
    public void initialize() {
        setupTable();
        loadBattles();
    }

    private void setupTable() {
        colRank.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getRank()));
        colUser.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
        colScore.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTotalScore() + " pts"));
        colSolved.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProblemsSolved() + " solved"));
    }

    private void loadBattles() {
        List<Battle> battles = battleService.getAllBattles();
        battleSelectCombo.setItems(FXCollections.observableArrayList(battles));
        if (!battles.isEmpty()) {
            battleSelectCombo.getSelectionModel().selectFirst();
            selectBattle(battles.get(0));
        }

        battleSelectCombo.setOnAction(e -> {
            Battle selected = battleSelectCombo.getValue();
            if (selected != null) {
                selectBattle(selected);
            }
        });
    }

    public void loadBattle(int battleId) {
        Battle b = battleService.getBattle(battleId);
        if (b != null) {
            battleSelectCombo.setValue(b);
            selectBattle(b);
        }
    }

    private void selectBattle(Battle battle) {
        this.activeBattle = battleService.getBattle(battle.getBattleId());
        battleTitleLabel.setText(activeBattle.getBattleName());
        battleStatusBadge.setText(activeBattle.getStatus().name());

        // Update badge color
        battleStatusBadge.getStyleClass().removeAll("badge-easy", "badge-medium", "badge-hard", "badge-live");
        if (activeBattle.getStatus() == BattleStatus.LIVE) {
            battleStatusBadge.getStyleClass().add("badge-live");
        } else {
            battleStatusBadge.getStyleClass().add("badge-medium");
        }

        // Initialize Timer
        initTimer(activeBattle.getDurationMinutes() * 60);

        // Render problem ribbon
        renderProblemRibbon();

        // Refresh scores
        refreshBattleLeaderboard();
    }

    private void initTimer(int totalSeconds) {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        this.remainingSeconds = totalSeconds;
        updateTimerDisplay();

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            remainingSeconds--;
            updateTimerDisplay();
            if (remainingSeconds <= 0) {
                countdownTimeline.stop();
                handleBattleTimeUp();
            }
        }));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void updateTimerDisplay() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));

        if (remainingSeconds < 180) { // < 3 minutes
            timerLabel.setStyle("-fx-text-fill: #f85149; -fx-border-color: #f85149;");
        } else {
            timerLabel.setStyle("-fx-text-fill: #e3b341; -fx-border-color: #d29922;");
        }
    }

    private void renderProblemRibbon() {
        problemsRibbon.getChildren().clear();
        if (activeBattle == null || activeBattle.getProblems() == null) return;

        Student s = SessionManager.getStudent();
        int studentId = s != null ? s.getUserId() : 0;
        Set<Integer> solvedIds = problemService.getSolvedProblemIdsForUser(studentId);

        List<Problem> problems = activeBattle.getProblems();
        int solvedInBattle = 0;

        for (int i = 0; i < problems.size(); i++) {
            Problem p = problems.get(i);
            boolean solved = solvedIds.contains(p.getProblemId());
            if (solved) solvedInBattle++;

            Button btn = new Button("P" + (i + 1) + ": " + p.getTitle() + (solved ? "  ✓" : ""));
            btn.getStyleClass().add(solved ? "btn-primary" : "button");
            btn.setStyle("-fx-font-size: 12px;");

            final int pId = p.getProblemId();
            btn.setOnAction(e -> {
                if (navigationCallback != null) {
                    navigationCallback.navigateTo("CODE_EDITOR", new Object[]{pId, activeBattle.getBattleId()});
                }
            });

            problemsRibbon.getChildren().add(btn);
        }

        solvedCountLabel.setText("Solved: " + solvedInBattle + " / " + problems.size());
    }

    private void refreshBattleLeaderboard() {
        if (activeBattle != null) {
            List<Score> scores = battleService.getBattleLeaderboard(activeBattle.getBattleId());
            battleScoreTable.setItems(FXCollections.observableArrayList(scores));
        }
    }

    private void handleBattleTimeUp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Battle Concluded!");
        alert.setHeaderText("Time's Up!");
        alert.setContentText("The battle countdown has concluded! Final scores and rankings are being calculated.");
        alert.show();
        refreshBattleLeaderboard();
    }
}
