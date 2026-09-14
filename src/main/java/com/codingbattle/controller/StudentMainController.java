package com.codingbattle.controller;

import com.codingbattle.model.Student;
import com.codingbattle.model.User;
import com.codingbattle.service.UserService;
import com.codingbattle.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StudentMainController implements NavigationCallback {

    @FXML private Label studentUsernameLabel;
    @FXML private Label studentPointsBadge;
    @FXML private StackPane contentArea;

    @FXML private Button navDashboardBtn;
    @FXML private Button navPracticeBtn;
    @FXML private Button navBattlesBtn;
    @FXML private Button navLeaderboardBtn;
    @FXML private Button navPerformanceBtn;

    private final UserService userService = new UserService();
    private final Map<String, Button> navButtonMap = new HashMap<>();

    @FXML
    public void initialize() {
        navButtonMap.put("DASHBOARD", navDashboardBtn);
        navButtonMap.put("PRACTICE", navPracticeBtn);
        navButtonMap.put("BATTLES", navBattlesBtn);
        navButtonMap.put("LEADERBOARD", navLeaderboardBtn);
        navButtonMap.put("PERFORMANCE", navPerformanceBtn);

        refreshUserProfile();
        navigateTo("DASHBOARD", null);
    }

    public void refreshUserProfile() {
        User u = SessionManager.getCurrentUser();
        if (u != null) {
            studentUsernameLabel.setText(u.getUsername());
            Student student = userService.getStudentProfile(u.getUserId());
            if (student != null) {
                studentPointsBadge.setText("⭐ " + student.getTotalPoints() + " pts");
            }
        }
    }

    @FXML
    private void handleNavDashboard(ActionEvent event) {
        navigateTo("DASHBOARD", null);
    }

    @FXML
    private void handleNavPractice(ActionEvent event) {
        navigateTo("PRACTICE", null);
    }

    @FXML
    private void handleNavBattles(ActionEvent event) {
        navigateTo("BATTLES", null);
    }

    @FXML
    private void handleNavLeaderboard(ActionEvent event) {
        navigateTo("LEADERBOARD", null);
    }

    @FXML
    private void handleNavPerformance(ActionEvent event) {
        navigateTo("PERFORMANCE", null);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.logout();
        try {
            Stage stage = (Stage) contentArea.getScene().getWindow();
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

    @Override
    public void navigateTo(String viewName, Object parameter) {
        // Highlight active nav button
        for (Map.Entry<String, Button> entry : navButtonMap.entrySet()) {
            if (entry.getValue() != null) {
                entry.getValue().getStyleClass().remove("sidebar-btn-active");
                if (entry.getKey().equalsIgnoreCase(viewName)) {
                    entry.getValue().getStyleClass().add("sidebar-btn-active");
                }
            }
        }

        refreshUserProfile();

        try {
            String fxmlPath;
            switch (viewName.toUpperCase()) {
                case "DASHBOARD" -> fxmlPath = "/fxml/StudentDashboardView.fxml";
                case "PRACTICE" -> fxmlPath = "/fxml/ProblemListView.fxml";
                case "BATTLES" -> fxmlPath = "/fxml/BattleArenaView.fxml";
                case "LEADERBOARD" -> fxmlPath = "/fxml/LeaderboardView.fxml";
                case "PERFORMANCE" -> fxmlPath = "/fxml/StudentPerformanceView.fxml";
                case "CODE_EDITOR" -> fxmlPath = "/fxml/CodeEditorView.fxml";
                default -> fxmlPath = "/fxml/StudentDashboardView.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node node = loader.load();
            Object controller = loader.getController();

            // Inject navigation callback and parameters
            if (controller instanceof StudentDashboardController c) {
                c.setNavigationCallback(this);
            } else if (controller instanceof ProblemListController c) {
                c.setNavigationCallback(this);
            } else if (controller instanceof CodeEditorController c) {
                c.setNavigationCallback(this);
                if (parameter instanceof Integer problemId) {
                    c.loadProblem(problemId, null);
                } else if (parameter instanceof Object[] arr && arr.length >= 2) {
                    c.loadProblem((Integer) arr[0], (Integer) arr[1]);
                }
            } else if (controller instanceof BattleArenaController c) {
                c.setNavigationCallback(this);
                if (parameter instanceof Integer battleId) {
                    c.loadBattle(battleId);
                }
            }

            contentArea.getChildren().setAll(node);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
