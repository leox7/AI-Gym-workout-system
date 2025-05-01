/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aigym.app.controllers;

import com.aigym.app.models.User;
import com.aigym.app.services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private UserService userService = new UserService();

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private Label errorMessageLabel;

    @FXML
    public void initialize() {
        // Initialize controller
        errorMessageLabel.setVisible(false);
    }

    @FXML
    private void handleLoginButtonAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showErrorMessage("Username and password cannot be empty");
            return;
        }
        
        try {
            User authenticatedUser = userService.authenticateUser(username, password);
            if (authenticatedUser != null) {
                logger.info("User logged in successfully: {}", username);
                loadDashboard(authenticatedUser);
            } else {
                showErrorMessage("Invalid username or password");
            }
        } catch (Exception e) {
            logger.error("Login error", e);
            showErrorMessage("An error occurred during login");
        }
    }
    
    @FXML
    private void handleRegisterLinkAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Parent registerView = loader.load();
            Scene registerScene = new Scene(registerView);
            
            Stage currentStage = (Stage) loginButton.getScene().getWindow();
            currentStage.setScene(registerScene);
            currentStage.setTitle("Register - AI Gym Workout System");
        } catch (IOException e) {
            logger.error("Error loading register view", e);
            showErrorMessage("Could not load registration page");
        }
    }
    
    private void loadDashboard(User user) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
        Parent dashboardView = loader.load();
        
        DashboardController dashboardController = loader.getController();
        dashboardController.initData(user);
        
        Scene dashboardScene = new Scene(dashboardView);
        
        Stage currentStage = (Stage) loginButton.getScene().getWindow();
        currentStage.setScene(dashboardScene);
        currentStage.setTitle("Dashboard - AI Gym Workout System");
        currentStage.setMaximized(true);
    }
    
    private void showErrorMessage(String message) {
        errorMessageLabel.setText(message);
        errorMessageLabel.setVisible(true);
    }
}
