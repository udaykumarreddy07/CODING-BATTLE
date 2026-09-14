package com.codingbattle;

/**
 * Main Launcher class without extending javafx.application.Application.
 * Prevents JavaFX runtime launcher issues when running as an executable JAR or from CLI.
 */
public class AppLauncher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
