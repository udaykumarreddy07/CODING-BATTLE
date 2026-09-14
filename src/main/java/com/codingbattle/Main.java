package com.codingbattle;

import com.codingbattle.database.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main JavaFX Application Class.
 * Initializes the database and displays the primary stage.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Initialize Database Schema & Seed Data
            DatabaseInitializer.initialize();

            // 2. Load Login View
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1200, 780);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            primaryStage.setTitle("Coding Battle Platform - Interactive Competition System");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(680);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
