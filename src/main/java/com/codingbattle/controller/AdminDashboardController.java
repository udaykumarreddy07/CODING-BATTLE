package com.codingbattle.controller;

import com.codingbattle.dao.TestCaseDAO;
import com.codingbattle.model.*;
import com.codingbattle.service.BattleService;
import com.codingbattle.service.ProblemService;
import com.codingbattle.service.SubmissionService;
import com.codingbattle.service.UserService;
import com.codingbattle.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminDashboardController {

    // Metrics
    @FXML private Label totalStudentsLabel;
    @FXML private Label totalProblemsLabel;
    @FXML private Label totalBattlesLabel;
    @FXML private Label totalSubmissionsLabel;

    // Problems Management
    @FXML private TableView<Problem> adminProblemsTable;
    @FXML private TableColumn<Problem, String> colProbId;
    @FXML private TableColumn<Problem, String> colProbTitle;
    @FXML private TableColumn<Problem, String> colProbCategory;
    @FXML private TableColumn<Problem, String> colProbDiff;
    @FXML private TableColumn<Problem, String> colProbPts;

    @FXML private TextField probTitleField;
    @FXML private TextArea probDescArea;
    @FXML private TextField probInputFormatField;
    @FXML private TextField probOutputFormatField;
    @FXML private TextField probConstraintsField;
    @FXML private ComboBox<Category> probCategoryCombo;
    @FXML private ComboBox<Difficulty> probDifficultyCombo;
    @FXML private TextField probPointsField;
    @FXML private TextField probTimeLimitField;
    @FXML private TextArea probSampleInputField;
    @FXML private TextArea probSampleOutputField;

    // Test Cases Management
    @FXML private ComboBox<Problem> tcProblemCombo;
    @FXML private TableView<TestCase> testCasesTable;
    @FXML private TableColumn<TestCase, String> colTcId;
    @FXML private TableColumn<TestCase, String> colTcInput;
    @FXML private TableColumn<TestCase, String> colTcExpected;
    @FXML private TableColumn<TestCase, String> colTcSample;
    @FXML private TextArea tcInputArea;
    @FXML private TextArea tcExpectedArea;
    @FXML private CheckBox tcSampleCheck;

    // Battles Management
    @FXML private TableView<Battle> adminBattlesTable;
    @FXML private TableColumn<Battle, String> colBatId;
    @FXML private TableColumn<Battle, String> colBatName;
    @FXML private TableColumn<Battle, String> colBatDuration;
    @FXML private TableColumn<Battle, String> colBatStatus;
    @FXML private TextField battleNameField;
    @FXML private TextField battleDurationField;
    @FXML private ComboBox<BattleStatus> battleStatusCombo;
    @FXML private ListView<Problem> battleProblemsListView;

    // Students Management
    @FXML private TableView<Student> adminStudentsTable;
    @FXML private TableColumn<Student, String> colStudId;
    @FXML private TableColumn<Student, String> colStudUsername;
    @FXML private TableColumn<Student, String> colStudEmail;
    @FXML private TableColumn<Student, String> colStudPoints;
    @FXML private TableColumn<Student, String> colStudSolved;

    // Submissions Audit
    @FXML private TableView<Submission> adminSubmissionsTable;
    @FXML private TableColumn<Submission, String> colSubId;
    @FXML private TableColumn<Submission, String> colSubUser;
    @FXML private TableColumn<Submission, String> colSubProblem;
    @FXML private TableColumn<Submission, String> colSubStatus;
    @FXML private TableColumn<Submission, String> colSubScore;
    @FXML private TableColumn<Submission, String> colSubTime;

    private final ProblemService problemService = new ProblemService();
    private final BattleService battleService = new BattleService();
    private final UserService userService = new UserService();
    private final SubmissionService submissionService = new SubmissionService();
    private final TestCaseDAO testCaseDAO = new TestCaseDAO();

    @FXML
    public void initialize() {
        setupTables();
        setupDropdowns();
        refreshAllData();
    }

    private void setupTables() {
        // Problems Table
        colProbId.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getProblemId()));
        colProbTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        colProbCategory.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategory().getDisplayName()));
        colProbDiff.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDifficulty().name()));
        colProbPts.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPoints() + " pts"));

        // Test Cases Table
        colTcId.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getTestId()));
        colTcInput.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getInputData()));
        colTcExpected.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getExpectedOutput()));
        colTcSample.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isSample() ? "Yes" : "No"));

        // Battles Table
        colBatId.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getBattleId()));
        colBatName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBattleName()));
        colBatDuration.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDurationMinutes() + " mins"));
        colBatStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));

        // Students Table
        colStudId.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getUserId()));
        colStudUsername.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
        colStudEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colStudPoints.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTotalPoints() + " pts"));
        colStudSolved.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSolvedProblems() + " solved"));

        // Submissions Table
        colSubId.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getSubmissionId()));
        colSubUser.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
        colSubProblem.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProblemTitle()));
        colSubStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().getDisplayName()));
        colSubScore.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getScore() + " pts"));
        colSubTime.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getSubmittedAt() != null ? d.getValue().getSubmittedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : ""
        ));
    }

    private void setupDropdowns() {
        probCategoryCombo.setItems(FXCollections.observableArrayList(Category.values()));
        probCategoryCombo.getSelectionModel().select(Category.ARRAYS);

        probDifficultyCombo.setItems(FXCollections.observableArrayList(Difficulty.values()));
        probDifficultyCombo.getSelectionModel().select(Difficulty.EASY);

        battleStatusCombo.setItems(FXCollections.observableArrayList(BattleStatus.values()));
        battleStatusCombo.getSelectionModel().select(BattleStatus.UPCOMING);

        battleProblemsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tcProblemCombo.setOnAction(e -> {
            Problem p = tcProblemCombo.getValue();
            if (p != null) {
                testCasesTable.setItems(FXCollections.observableArrayList(testCaseDAO.getTestCasesByProblemId(p.getProblemId())));
            }
        });
    }

    public void refreshAllData() {
        List<Problem> problems = problemService.getAllProblems();
        List<Student> students = userService.getAllStudents();
        List<Battle> battles = battleService.getAllBattles();
        List<Submission> submissions = submissionService.getAllSubmissions();

        // Update metric cards
        totalProblemsLabel.setText(String.valueOf(problems.size()));
        totalStudentsLabel.setText(String.valueOf(students.size()));
        totalBattlesLabel.setText(String.valueOf(battles.size()));
        totalSubmissionsLabel.setText(String.valueOf(submissions.size()));

        // Populate tables
        adminProblemsTable.setItems(FXCollections.observableArrayList(problems));
        adminStudentsTable.setItems(FXCollections.observableArrayList(students));
        adminBattlesTable.setItems(FXCollections.observableArrayList(battles));
        adminSubmissionsTable.setItems(FXCollections.observableArrayList(submissions));

        tcProblemCombo.setItems(FXCollections.observableArrayList(problems));
        if (!problems.isEmpty()) {
            tcProblemCombo.getSelectionModel().selectFirst();
            testCasesTable.setItems(FXCollections.observableArrayList(testCaseDAO.getTestCasesByProblemId(problems.get(0).getProblemId())));
        }

        battleProblemsListView.setItems(FXCollections.observableArrayList(problems));
    }

    @FXML
    private void handleAddProblem(ActionEvent event) {
        String title = probTitleField.getText();
        String desc = probDescArea.getText();
        if (title == null || title.trim().isEmpty() || desc == null || desc.trim().isEmpty()) {
            showAlert("Validation Error", "Problem Title and Description are required.");
            return;
        }

        int points = 100;
        try {
            points = Integer.parseInt(probPointsField.getText().trim());
        } catch (Exception ignored) {}

        int timeLimit = 2000;
        try {
            timeLimit = Integer.parseInt(probTimeLimitField.getText().trim());
        } catch (Exception ignored) {}

        Problem p = new Problem(
                0,
                title.trim(),
                desc.trim(),
                probInputFormatField.getText(),
                probOutputFormatField.getText(),
                probConstraintsField.getText(),
                probDifficultyCombo.getValue(),
                probCategoryCombo.getValue(),
                points,
                timeLimit,
                probSampleInputField.getText(),
                probSampleOutputField.getText()
        );

        int id = problemService.createProblem(p);
        if (id > 0) {
            showAlert("Success", "Problem created successfully with ID #" + id);
            clearProblemForm();
            refreshAllData();
        } else {
            showAlert("Error", "Failed to create problem in database.");
        }
    }

    @FXML
    private void handleDeleteProblem(ActionEvent event) {
        Problem selected = adminProblemsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Required", "Please select a problem to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete problem: " + selected.getTitle() + "?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            problemService.deleteProblem(selected.getProblemId());
            refreshAllData();
        }
    }

    @FXML
    private void handleAddTestCase(ActionEvent event) {
        Problem selected = tcProblemCombo.getValue();
        if (selected == null) {
            showAlert("Selection Required", "Please select a problem first.");
            return;
        }

        String input = tcInputArea.getText();
        String expected = tcExpectedArea.getText();
        if (expected == null || expected.trim().isEmpty()) {
            showAlert("Validation Error", "Expected output cannot be empty.");
            return;
        }

        TestCase tc = new TestCase(selected.getProblemId(), input != null ? input : "", expected.trim(), tcSampleCheck.isSelected());
        if (testCaseDAO.addTestCase(tc)) {
            tcInputArea.clear();
            tcExpectedArea.clear();
            tcSampleCheck.setSelected(false);
            testCasesTable.setItems(FXCollections.observableArrayList(testCaseDAO.getTestCasesByProblemId(selected.getProblemId())));
        }
    }

    @FXML
    private void handleDeleteTestCase(ActionEvent event) {
        TestCase selected = testCasesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            testCaseDAO.deleteTestCase(selected.getTestId());
            Problem p = tcProblemCombo.getValue();
            if (p != null) {
                testCasesTable.setItems(FXCollections.observableArrayList(testCaseDAO.getTestCasesByProblemId(p.getProblemId())));
            }
        }
    }

    @FXML
    private void handleCreateBattle(ActionEvent event) {
        String name = battleNameField.getText();
        if (name == null || name.trim().isEmpty()) {
            showAlert("Validation Error", "Battle Name is required.");
            return;
        }

        int duration = 30;
        try {
            duration = Integer.parseInt(battleDurationField.getText().trim());
        } catch (Exception ignored) {}

        List<Problem> selectedProbs = battleProblemsListView.getSelectionModel().getSelectedItems();
        List<Integer> probIds = new ArrayList<>();
        for (Problem p : selectedProbs) {
            probIds.add(p.getProblemId());
        }

        Battle battle = new Battle(0, name.trim(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(duration), duration, battleStatusCombo.getValue(), 100);
        int bId = battleService.createBattle(battle, probIds);
        if (bId > 0) {
            showAlert("Success", "Battle '" + name + "' created successfully!");
            battleNameField.clear();
            refreshAllData();
        }
    }

    @FXML
    private void handleDeleteBattle(ActionEvent event) {
        Battle selected = adminBattlesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            battleService.removeBattle(selected.getBattleId());
            refreshAllData();
        }
    }

    @FXML
    private void handleDeleteStudent(ActionEvent event) {
        Student selected = adminStudentsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete student " + selected.getUsername() + "?", ButtonType.YES, ButtonType.NO);
            if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                userService.removeStudent(selected.getUserId());
                refreshAllData();
            }
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.logout();
        try {
            Stage stage = (Stage) totalStudentsLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 780);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setTitle("Coding Battle Platform - Login");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearProblemForm() {
        probTitleField.clear();
        probDescArea.clear();
        probInputFormatField.clear();
        probOutputFormatField.clear();
        probConstraintsField.clear();
        probPointsField.setText("100");
        probTimeLimitField.setText("2000");
        probSampleInputField.clear();
        probSampleOutputField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
