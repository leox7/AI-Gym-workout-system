/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aigym.app.controllers;

import com.aigym.app.models.User;
import com.aigym.app.services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangePasswordController {
    private static final Logger logger = LoggerFactory.getLogger(ChangePasswordController.class);
    private UserService userService = new UserService();
    private User currentUser;
    
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    public void initData(User user) {
        this.currentUser = user;
    }
    
    @FXML
    private void handleSaveButtonAction(ActionEvent event) {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validate inputs
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields are required");
            return;
        }
        
        // Verify current password
        if (!userService.verifyPassword(currentUser.getUsername(), currentPassword)) {
            showAlert(Alert.AlertType.ERROR, "Authentication Error", "Current password is incorrect");
            return;
        }
        
        // Check if new passwords match
        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "New passwords do not match");
            return;
        }
        
        // Validate password strength
        if (newPassword.length() < 8) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Password must be at least 8 characters long");
            return;
        }
        
        // Update password
        try {
            userService.updatePassword(currentUser.getId(), newPassword);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated successfully");
            closeDialog();
        } catch (Exception e) {
            logger.error("Error updating password", e);
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while updating password: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        closeDialog();
    }
    
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
