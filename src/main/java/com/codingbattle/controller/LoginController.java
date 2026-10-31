package com.codingbattle.controller;

import com.codingbattle.database.DatabaseConnection;
import com.codingbattle.model.User;
import com.codingbattle.service.UserService;
import com.codingbattle.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    // Student Login Tab
    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;
    @FXML private Label loginMessageLabel;

    // Faculty Login Tab
    @FXML private TextField facultyUsernameField;
    @FXML private PasswordField facultyPasswordField;
    @FXML private Label facultyMessageLabel;

    // Student Registration Tab
    @FXML private TextField regUsernameField;
    @FXML private TextField regEmailField;
    @FXML private PasswordField regPasswordField;
    @FXML private PasswordField regConfirmPasswordField;
    @FXML private Label regMessageLabel;

    @FXML private Label dbStatusLabel;
    @FXML private TabPane authTabPane;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        String dbType = DatabaseConnection.getInstance().getActiveDatabaseType();
        if (dbStatusLabel != null) {
            dbStatusLabel.setText("Database: " + dbType + " (Connected)");
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = loginUsernameField.getText();
        String password = loginPasswordField.getText();

        try {
            User user = userService.login(username, password);
            SessionManager.setCurrentUser(user);

            // Navigate based on role
            Stage stage = (Stage) loginUsernameField.getScene().getWindow();
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                loadView(stage, "/fxml/AdminDashboardView.fxml", "Coding Battle Platform - Faculty Command Portal");
            } else {
                loadView(stage, "/fxml/StudentMainView.fxml", "Coding Battle Platform - Student Arena");
            }

        } catch (IllegalArgumentException ex) {
            loginMessageLabel.setText(ex.getMessage());
            loginMessageLabel.setStyle("-fx-text-fill: #f85149;");
        } catch (Exception ex) {
            loginMessageLabel.setText("Login Error: " + ex.getMessage());
            loginMessageLabel.setStyle("-fx-text-fill: #f85149;");
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleFacultyLogin(ActionEvent event) {
        String username = facultyUsernameField != null ? facultyUsernameField.getText() : "";
        String password = facultyPasswordField != null ? facultyPasswordField.getText() : "";

        try {
            User user = userService.login(username, password);
            if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                if (facultyMessageLabel != null) {
                    facultyMessageLabel.setText("⛔ Access Denied: Faculty credentials required. Please use Student Login.");
                    facultyMessageLabel.setStyle("-fx-text-fill: #f85149;");
                }
                return;
            }

            SessionManager.setCurrentUser(user);
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            loadView(stage, "/fxml/AdminDashboardView.fxml", "Coding Battle Platform - Faculty Command Portal");

        } catch (Exception ex) {
            if (facultyMessageLabel != null) {
                facultyMessageLabel.setText("Authentication failed: " + ex.getMessage());
                facultyMessageLabel.setStyle("-fx-text-fill: #f85149;");
            }
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = regUsernameField.getText();
        String email = regEmailField.getText();
        String password = regPasswordField.getText();
        String confirm = regConfirmPasswordField.getText();

        if (password == null || !password.equals(confirm)) {
            regMessageLabel.setText("Passwords do not match.");
            regMessageLabel.setStyle("-fx-text-fill: #f85149;");
            return;
        }

        try {
            userService.registerStudent(username, email, password);
            regMessageLabel.setText("Registration successful! Please sign in.");
            regMessageLabel.setStyle("-fx-text-fill: #3fb950;");

            // Fill login tab
            loginUsernameField.setText(username);
            loginPasswordField.setText(password);
            authTabPane.getSelectionModel().select(0);

        } catch (Exception ex) {
            regMessageLabel.setText(ex.getMessage());
            regMessageLabel.setStyle("-fx-text-fill: #f85149;");
        }
    }

    @FXML
    private void fillStudentDemo() {
        loginUsernameField.setText("rahul");
        loginPasswordField.setText("student123");
        loginMessageLabel.setText("Loaded student credentials (rahul / student123)");
        loginMessageLabel.setStyle("-fx-text-fill: #58a6ff;");
    }

    @FXML
    private void fillFacultyDemo() {
        if (facultyUsernameField != null) facultyUsernameField.setText("admin");
        if (facultyPasswordField != null) facultyPasswordField.setText("admin123");
        if (facultyMessageLabel != null) {
            facultyMessageLabel.setText("Loaded faculty credentials (admin / admin123)");
            facultyMessageLabel.setStyle("-fx-text-fill: #58a6ff;");
        }
    }

    @FXML
    private void fillAdminDemo() {
        fillFacultyDemo();
        // Also support fallback in student tab if clicked
        if (loginUsernameField != null) loginUsernameField.setText("admin");
        if (loginPasswordField != null) loginPasswordField.setText("admin123");
    }

    private void loadView(Stage stage, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 780);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setTitle(title);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            if (loginMessageLabel != null) {
                loginMessageLabel.setText("Error loading view: " + e.getMessage());
                loginMessageLabel.setStyle("-fx-text-fill: #f85149;");
            }
        }
    }
}
