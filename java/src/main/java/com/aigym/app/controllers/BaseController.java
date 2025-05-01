/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aigym.app.controllers;

import com.aigym.app.models.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Base controller class that provides common functionality for all controllers.
 */
public abstract class BaseController {
    protected static final Logger logger = LoggerFactory.getLogger(BaseController.class);
    protected User currentUser;
    
    /**
     * Initialize the controller with user data.
     * 
     * @param user The current user
     */
    public void initData(User user) {
        this.currentUser = user;
    }
    
    /**
     * Navigate to the dashboard view.
     */
    protected void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent dashboardView = loader.load();
            
            DashboardController dashboardController = loader.getController();
            dashboardController.initData(currentUser);
            
            setScene(dashboardView, "Dashboard - AI Gym Workout System");
        } catch (IOException e) {
            logger.error("Error navigating to dashboard", e);
            showAlert(AlertType.ERROR, "Navigation Error", "Could not navigate to dashboard");
        }
    }
    
    /**
     * Set the scene with the provided parent node.
     * 
     * @param root The root node of the new scene
     * @param title The title for the stage
     */
    protected void setScene(Parent root, String title) {
        Scene scene = new Scene(root);
        Stage stage = getStage();
        stage.setScene(scene);
        stage.setTitle(title);
    }
    
    /**
     * Get the current stage.
     * 
     * @return The current stage
     */
    protected Stage getStage() {
        // This assumes that the controller has at least one JavaFX node
        // that can be used to get the scene and window
        return (Stage) getRoot().getScene().getWindow();
    }
    
    /**
     * Get the root node of this controller's view.
     * This method must be implemented by subclasses to return a node
     * that is part of the scene graph.
     * 
     * @return A node that is part of the scene graph
     */
    protected abstract Parent getRoot();
    
    /**
     * Show an alert dialog.
     * 
     * @param type The type of alert
     * @param title The title of the alert
     * @param content The content of the alert
     */
    protected void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}