package com.codingbattle.controller;

import com.codingbattle.dsa.LeaderboardHeap;
import com.codingbattle.dsa.QuickSortRanking;
import com.codingbattle.model.LeaderboardEntry;
import com.codingbattle.service.LeaderboardService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

public class LeaderboardController {

    @FXML private VBox firstPlaceCard;
    @FXML private Label firstPlaceUser;
    @FXML private Label firstPlacePoints;

    @FXML private VBox secondPlaceCard;
    @FXML private Label secondPlaceUser;
    @FXML private Label secondPlacePoints;

    @FXML private VBox thirdPlaceCard;
    @FXML private Label thirdPlaceUser;
    @FXML private Label thirdPlacePoints;

    @FXML private TextField searchStudentField;
    @FXML private ComboBox<String> topKCombo;

    @FXML private TableView<LeaderboardEntry> leaderboardTable;
    @FXML private TableColumn<LeaderboardEntry, String> colRank;
    @FXML private TableColumn<LeaderboardEntry, String> colStudent;
    @FXML private TableColumn<LeaderboardEntry, String> colPoints;
    @FXML private TableColumn<LeaderboardEntry, String> colSolved;
    @FXML private TableColumn<LeaderboardEntry, String> colSuccessRate;

    private final LeaderboardService leaderboardService = new LeaderboardService();

    @FXML
    public void initialize() {
        setupTable();
        setupControls();
        loadLeaderboard();
    }

    private void setupTable() {
        colRank.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getRank()));
        colStudent.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
        colPoints.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTotalPoints() + " pts"));
        colSolved.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProblemsSolved() + " solved"));
        colSuccessRate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSuccessRate() + "%"));
    }

    private void setupControls() {
        topKCombo.getItems().addAll("Top 5 (Heap)", "Top 10 (Heap)", "All Students (QuickSort)");
        topKCombo.getSelectionModel().selectFirst();

        searchStudentField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                List<LeaderboardEntry> filtered = leaderboardService.searchLeaderboard(newVal);
                leaderboardTable.setItems(FXCollections.observableArrayList(filtered));
            } else {
                loadLeaderboard();
            }
        });

        topKCombo.setOnAction(e -> loadLeaderboard());
    }

    private void loadLeaderboard() {
        String mode = topKCombo.getValue();
        List<LeaderboardEntry> entries;

        if ("Top 5 (Heap)".equals(mode)) {
            entries = leaderboardService.getTopKLeaders(5);
        } else if ("Top 10 (Heap)".equals(mode)) {
            entries = leaderboardService.getTopKLeaders(10);
        } else {
            entries = leaderboardService.getGlobalLeaderboard();
        }

        leaderboardTable.setItems(FXCollections.observableArrayList(entries));

        // Populate Top 3 Podium
        List<LeaderboardEntry> top3 = leaderboardService.getTopKLeaders(3);
        if (top3.size() >= 1) {
            firstPlaceUser.setText("🥇 " + top3.get(0).getUsername());
            firstPlacePoints.setText(top3.get(0).getTotalPoints() + " pts (" + top3.get(0).getProblemsSolved() + " solved)");
        } else {
            firstPlaceUser.setText("---");
            firstPlacePoints.setText("0 pts");
        }

        if (top3.size() >= 2) {
            secondPlaceUser.setText("🥈 " + top3.get(1).getUsername());
            secondPlacePoints.setText(top3.get(1).getTotalPoints() + " pts (" + top3.get(1).getProblemsSolved() + " solved)");
        } else {
            secondPlaceUser.setText("---");
            secondPlacePoints.setText("0 pts");
        }

        if (top3.size() >= 3) {
            thirdPlaceUser.setText("🥉 " + top3.get(2).getUsername());
            thirdPlacePoints.setText(top3.get(2).getTotalPoints() + " pts (" + top3.get(2).getProblemsSolved() + " solved)");
        } else {
            thirdPlaceUser.setText("---");
            thirdPlacePoints.setText("0 pts");
        }
    }
}
