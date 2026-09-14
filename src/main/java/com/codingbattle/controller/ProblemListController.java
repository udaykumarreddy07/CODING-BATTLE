package com.codingbattle.controller;

import com.codingbattle.model.Category;
import com.codingbattle.model.Difficulty;
import com.codingbattle.model.Problem;
import com.codingbattle.model.Student;
import com.codingbattle.service.ProblemService;
import com.codingbattle.util.SessionManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.List;

public class ProblemListController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ComboBox<String> difficultyCombo;
    @FXML private ComboBox<String> statusCombo;

    @FXML private TableView<Problem> problemsTable;
    @FXML private TableColumn<Problem, String> colStatus;
    @FXML private TableColumn<Problem, String> colTitle;
    @FXML private TableColumn<Problem, String> colCategory;
    @FXML private TableColumn<Problem, Difficulty> colDifficulty;
    @FXML private TableColumn<Problem, String> colPoints;
    @FXML private TableColumn<Problem, Problem> colAction;

    @FXML private Label countLabel;

    private NavigationCallback navigationCallback;
    private final ProblemService problemService = new ProblemService();

    public void setNavigationCallback(NavigationCallback callback) {
        this.navigationCallback = callback;
    }

    @FXML
    public void initialize() {
        setupFilterControls();
        setupTable();
        loadProblems();
    }

    private void setupFilterControls() {
        categoryCombo.getItems().add("All Categories");
        for (Category c : Category.values()) {
            categoryCombo.getItems().add(c.getDisplayName());
        }
        categoryCombo.getSelectionModel().selectFirst();

        difficultyCombo.getItems().addAll("All Difficulties", "EASY", "MEDIUM", "HARD");
        difficultyCombo.getSelectionModel().selectFirst();

        statusCombo.getItems().addAll("All Status", "Solved", "Unsolved");
        statusCombo.getSelectionModel().selectFirst();

        // Add change listeners for live filtering
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        categoryCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        difficultyCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void setupTable() {
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().isSolvedByCurrentUser() ? "✓ Solved" : "○ Unsolved"
        ));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("Solved")) {
                        setStyle("-fx-text-fill: #3fb950; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #8b949e;");
                    }
                }
            }
        });

        colTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        colCategory.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory().getDisplayName()));

        colDifficulty.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDifficulty()));
        colDifficulty.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Difficulty item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item.name());
                    badge.getStyleClass().add("badge-pill");
                    switch (item) {
                        case EASY -> badge.getStyleClass().add("badge-easy");
                        case MEDIUM -> badge.getStyleClass().add("badge-medium");
                        case HARD -> badge.getStyleClass().add("badge-hard");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        colPoints.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPoints() + " pts"));

        colAction.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button solveBtn = new Button("Solve Challenge");
            {
                solveBtn.getStyleClass().add("btn-accent");
                solveBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 10 4 10;");
                solveBtn.setOnAction(e -> {
                    Problem p = getItem();
                    if (p != null && navigationCallback != null) {
                        navigationCallback.navigateTo("CODE_EDITOR", p.getProblemId());
                    }
                });
            }

            @Override
            protected void updateItem(Problem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(solveBtn);
                }
            }
        });
    }

    private void loadProblems() {
        Student s = SessionManager.getStudent();
        int studentId = s != null ? s.getUserId() : 0;
        List<Problem> list = problemService.getAllProblemsForStudent(studentId);
        problemsTable.setItems(FXCollections.observableArrayList(list));
        countLabel.setText("Showing " + list.size() + " challenges");
    }

    private void applyFilters() {
        Student s = SessionManager.getStudent();
        int studentId = s != null ? s.getUserId() : 0;

        String query = searchField.getText();
        String catVal = categoryCombo.getValue();
        Category cat = (catVal != null && !"All Categories".equals(catVal)) ? Category.fromString(catVal) : null;

        String diffVal = difficultyCombo.getValue();
        Difficulty diff = (diffVal != null && !"All Difficulties".equals(diffVal)) ? Difficulty.fromString(diffVal) : null;

        String statVal = statusCombo.getValue();
        Boolean solvedOnly = null;
        if ("Solved".equals(statVal)) solvedOnly = true;
        else if ("Unsolved".equals(statVal)) solvedOnly = false;

        List<Problem> filtered = problemService.searchAndFilter(query, cat, diff, solvedOnly, studentId);
        problemsTable.setItems(FXCollections.observableArrayList(filtered));
        countLabel.setText("Showing " + filtered.size() + " challenges");
    }

    @FXML
    private void handleResetFilters(ActionEvent event) {
        searchField.clear();
        categoryCombo.getSelectionModel().selectFirst();
        difficultyCombo.getSelectionModel().selectFirst();
        statusCombo.getSelectionModel().selectFirst();
        loadProblems();
    }
}
