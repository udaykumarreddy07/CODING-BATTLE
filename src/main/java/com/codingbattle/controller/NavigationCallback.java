package com.codingbattle.controller;

/**
 * Interface allowing nested controllers to request navigation changes in the student main layout.
 */
public interface NavigationCallback {
    void navigateTo(String viewName, Object parameter);
}
