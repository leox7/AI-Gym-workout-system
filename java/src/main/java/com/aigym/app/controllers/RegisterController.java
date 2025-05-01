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
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RegisterController {
    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);
    private UserService userService = new UserService();

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Button backButton;
    @FXML private Label errorMessageLabel;

    @FXML
    public void initialize() {
        errorMessageLabel.setVisible(false);
    }

    @FXML
    private void handleRegisterButtonAction(ActionEvent event) {
        // Clear previous error messages
        errorMessageLabel.setVisible(false);
        
        // Get form values
        String fullName = fullNameField.getText();
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validate input
        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || 
            password.isEmpty() || confirmPassword.isEmpty()) {
            showErrorMessage("All fields are required");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showErrorMessage("Passwords do not match");
            return;
        }
        
        // Parse full name into first and last name
        String firstName = fullName;
        String lastName = "";
        if (fullName.contains(" ")) {
            String[] nameParts = fullName.split(" ", 2);
            firstName = nameParts[0];
            lastName = nameParts[1];
        }
        
        // Create User object
        User newUser = new User(username, email, "", User.UserRole.MEMBER);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        
        // Register the user
        try {
            User registeredUser = userService.registerUser(newUser, password);
            if (registeredUser != null) {
                logger.info("User registered successfully: {}", username);
                showLoginScreen();
            } else {
                showErrorMessage("Registration failed. Please try again.");
            }
        } catch (Exception e) {
            logger.error("Registration error", e);
            showErrorMessage("An error occurred during registration: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleBackButtonAction(ActionEvent event) {
        showLoginScreen();
    }
    
    private void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent loginView = loader.load();
            Scene loginScene = new Scene(loginView);
            
            Stage currentStage = (Stage) registerButton.getScene().getWindow();
            currentStage.setScene(loginScene);
            currentStage.setTitle("Login - AI Gym Workout System");
        } catch (IOException e) {
            logger.error("Error loading login view", e);
            showErrorMessage("Could not return to login page");
        }
    }
    
    private void showErrorMessage(String message) {
        errorMessageLabel.setText(message);
        errorMessageLabel.setVisible(true);
    }
}
