/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aigym.app.controllers;

import com.aigym.app.models.User;
import com.aigym.app.services.UserService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;

public class ProfileController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    private UserService userService = new UserService();
    
    @FXML private Button dashboardButton;
    @FXML private Button workoutsButton;
    @FXML private Button progressButton;
    @FXML private Button profileButton;
    @FXML private Button logoutButton;
    @FXML private Button saveButton;
    
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private DatePicker dateOfBirthPicker;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private TextField heightField;
    @FXML private TextField weightField;
    @FXML private ComboBox<String> fitnessGoalComboBox;
    @FXML private ComboBox<String> fitnessLevelComboBox;
    @FXML private TextField usernameField;
    
    @FXML
    public void initialize() {
        // Initialize combo boxes
        genderComboBox.setItems(FXCollections.observableArrayList("Male", "Female","Prefer not to say"));
        
        fitnessGoalComboBox.setItems(FXCollections.observableArrayList(
                "Weight Loss", "Muscle Gain", "Endurance", "Flexibility", "General Fitness"));
        
        fitnessLevelComboBox.setItems(FXCollections.observableArrayList(
                "Beginner", "Intermediate", "Advanced", "Professional"));
        
        // Add numeric validation to height and weight fields
        heightField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                heightField.setText(oldValue);
            }
        });
        
        weightField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                weightField.setText(oldValue);
            }
        });
    }
    
    @Override
    public void initData(User user) {
        super.initData(user);
        populateFields();
    }
    
    private void populateFields() {
        // Populate form fields with user data
        firstNameField.setText(currentUser.getFirstName());
        lastNameField.setText(currentUser.getLastName());
        emailField.setText(currentUser.getEmail());
        
        if (currentUser.getDateOfBirth() != null) {
            dateOfBirthPicker.setValue(currentUser.getDateOfBirth());
        }
        
        if (currentUser.getGender() != null) {
            genderComboBox.setValue(currentUser.getGender());
        }
        
        heightField.setText(String.valueOf(currentUser.getHeight()));
        weightField.setText(String.valueOf(currentUser.getWeight()));
        
        if (currentUser.getFitnessGoal() != null) {
            fitnessGoalComboBox.setValue(currentUser.getFitnessGoal());
        }
        
        if (currentUser.getFitnessLevel() != null) {
            fitnessLevelComboBox.setValue(currentUser.getFitnessLevel());
        }
        
        usernameField.setText(currentUser.getUsername());
    }
    
    @FXML
    private void handleSaveButtonAction(ActionEvent event) {
        try {
            // Validate required fields
            if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() || emailField.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "First name, last name, and email are required");
                return;
            }
            
            // Update user object with form data
            currentUser.setFirstName(firstNameField.getText());
            currentUser.setLastName(lastNameField.getText());
            currentUser.setEmail(emailField.getText());
            currentUser.setDateOfBirth(dateOfBirthPicker.getValue());
            currentUser.setGender(genderComboBox.getValue());
            
            if (!heightField.getText().isEmpty()) {
                currentUser.setHeight(Double.parseDouble(heightField.getText()));
            }
            
            if (!weightField.getText().isEmpty()) {
                currentUser.setWeight(Double.parseDouble(weightField.getText()));
            }
            
            currentUser.setFitnessGoal(fitnessGoalComboBox.getValue());
            currentUser.setFitnessLevel(fitnessLevelComboBox.getValue());
            
            // Save user data
            boolean updateSuccess = userService.updateUser(currentUser);
            
            if (updateSuccess) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully");
                // No need to update the current user as we're already using the updated object
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update profile");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Height and weight must be valid numbers");
        } catch (Exception e) {
            logger.error("Error saving profile", e);
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while saving profile: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleChangePasswordButtonAction(ActionEvent event) {
        try {
            // Load the change password dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/change_password.fxml"));
            Parent changePasswordView = loader.load();
            
            ChangePasswordController controller = loader.getController();
            controller.initData(currentUser);
            
            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Change Password");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(saveButton.getScene().getWindow());
            dialogStage.setScene(new Scene(changePasswordView));
            
            // Show the dialog and wait for it to close
            dialogStage.showAndWait();
        } catch (IOException e) {
            logger.error("Error loading change password dialog", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open change password dialog");
        }
    }
    
    @FXML
    private void handleDashboardButtonAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent dashboardView = loader.load();
            
            DashboardController dashboardController = loader.getController();
            dashboardController.initData(currentUser);
            
            Scene dashboardScene = new Scene(dashboardView);
            Stage currentStage = (Stage) dashboardButton.getScene().getWindow();
            currentStage.setScene(dashboardScene);
            currentStage.setTitle("Dashboard - AI Gym Workout System");
        } catch (IOException e) {
            logger.error("Error loading dashboard view", e);
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load dashboard page");
        }
    }
    
    @FXML
    private void handleWorkoutsButtonAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/workouts.fxml"));
            Parent workoutsView = loader.load();
            
            WorkoutsController workoutsController = loader.getController();
            workoutsController.initData(currentUser);
            
            Scene workoutsScene = new Scene(workoutsView);
            Stage currentStage = (Stage) workoutsButton.getScene().getWindow();
            currentStage.setScene(workoutsScene);
            currentStage.setTitle("Workouts - AI Gym Workout System");
        } catch (IOException e) {
            logger.error("Error loading workouts view", e);
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load workouts page");
        }
    }
    
    @FXML
    private void handleProgressButtonAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/progress.fxml"));
            Parent progressView = loader.load();
            
            ProgressController progressController = loader.getController();
            progressController.initData(currentUser);
            
            Scene progressScene = new Scene(progressView);
            Stage currentStage = (Stage) progressButton.getScene().getWindow();
            currentStage.setScene(progressScene);
            currentStage.setTitle("Progress - AI Gym Workout System");
        } catch (IOException e) {
            logger.error("Error loading progress view", e);
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load progress page");
        }
    }
    
    @FXML
    private void handleProfileButtonAction(ActionEvent event) {
        // Already on profile page, no action needed
    }
    
    @FXML
    private void handleLogoutButtonAction(ActionEvent event) {
        try {
            // Return to login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent loginView = loader.load();
            Scene loginScene = new Scene(loginView);
            
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.setScene(loginScene);
            currentStage.setTitle("Login - AI Gym Workout System");
            currentStage.setMaximized(false);
            currentStage.centerOnScreen();
            
            logger.info("User logged out: {}", currentUser.getUsername());
        } catch (IOException e) {
            logger.error("Error returning to login screen", e);
            showAlert(Alert.AlertType.ERROR, "Logout Error", "Could not log out properly");
        }
    }
    
    @Override
    protected Parent getRoot() {
        return dashboardButton.getParent().getParent();
    }
}
