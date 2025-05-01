/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aigym.app.controllers;

import com.aigym.app.models.Workout;
import com.aigym.app.models.User;
import com.aigym.app.services.WorkoutService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class WorkoutCardController {
    private static final Logger logger = LoggerFactory.getLogger(WorkoutCardController.class);
    private Workout workout;
    private User currentUser;
    private WorkoutService workoutService = new WorkoutService();
    
    @FXML private Label workoutNameLabel;
    @FXML private Label workoutDescriptionLabel;
    @FXML private Label workoutDurationLabel;
    @FXML private Label workoutDifficultyLabel;
    @FXML private Label workoutTargetMuscleGroupLabel;
    @FXML private Label workoutAiGeneratedLabel;
    @FXML private Button startWorkoutButton;
    @FXML private Button favoriteButton;
    
    @FXML
    public void initialize() {
        // This method is automatically called after the FXML is loaded
    }
    
    public void setWorkout(Workout workout) {
        this.workout = workout;
        
        // Update UI with workout data
        workoutNameLabel.setText(workout.getName());
        workoutDescriptionLabel.setText(workout.getDescription());
        workoutDurationLabel.setText(workout.getDuration() + " minutes");
        workoutDifficultyLabel.setText(workout.getDifficulty());
        
        // Set target muscle group if available
        if (workout.getTargetMuscleGroup() != null && !workout.getTargetMuscleGroup().isEmpty()) {
            workoutTargetMuscleGroupLabel.setText(workout.getTargetMuscleGroup());
            workoutTargetMuscleGroupLabel.setVisible(true);
        } else {
            workoutTargetMuscleGroupLabel.setVisible(false);
        }
        
        // Set AI generated indicator if applicable
        if (workout.isAiGenerated()) {
            workoutAiGeneratedLabel.setText("AI Generated");
            workoutAiGeneratedLabel.setVisible(true);
        } else {
            workoutAiGeneratedLabel.setVisible(false);
        }
        
        // Set favorite button text based on workout's favorite status
        updateFavoriteButtonText();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    @FXML
    private void handleStartWorkoutButtonAction() {
        logger.info("Starting workout: {}", workout.getName());
        
        try {
            // Load the workout details FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/workout_details.fxml"));
            Parent workoutDetailsView = loader.load();
            
            // Get the controller and pass the workout data
            WorkoutDetailsController detailsController = loader.getController();
            detailsController.setWorkout(workout);
            detailsController.setCurrentUser(currentUser);
            
            // Create a new scene and set it on the current stage
            Scene workoutDetailsScene = new Scene(workoutDetailsView);
            Stage currentStage = (Stage) startWorkoutButton.getScene().getWindow();
            currentStage.setScene(workoutDetailsScene);
            currentStage.setTitle(workout.getName() + " - AI Gym Workout System");
            
            logger.info("Navigated to workout details for: {}", workout.getName());
        } catch (IOException e) {
            logger.error("Error navigating to workout details: {}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open workout details. Please try again.");
        }
    }
    
    @FXML
    private void handleFavoriteButtonAction() {
        if (currentUser == null) {
            logger.error("Cannot toggle favorite: current user is null");
            showAlert(Alert.AlertType.ERROR, "Error", "You must be logged in to favorite workouts");
            return;
        }
        
        // If the workout doesn't have a valid ID (e.g., it's a recommended workout from the API),
        // save it to the database first
        if (workout.getId() <= 0) {
            try {
                logger.info("Saving recommended workout to database before adding to favorites: {}", workout.getName());
                workout = workoutService.createWorkout(workout, currentUser.getId());
                
                if (workout.getId() <= 0) {
                    logger.error("Failed to save workout to database: {}", workout.getName());
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to save workout to database. Please try again later.");
                    return;
                }
                
                logger.info("Successfully saved workout to database with ID: {}", workout.getId());
            } catch (Exception e) {
                logger.error("Error saving workout to database: {}", e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save workout to database. Please try again later.");
                return;
            }
        }
        
        // Toggle favorite status locally first for immediate UI feedback
        boolean newFavoriteStatus = !workout.isFavorite();
        workout.setFavorite(newFavoriteStatus);
        updateFavoriteButtonText();
        
        // Update the database
        boolean success = workoutService.toggleFavoriteWorkout(currentUser.getId(), workout.getId(), newFavoriteStatus);
        
        if (success) {
            logger.info("Successfully toggled favorite status for workout {}: {}", 
                    workout.getName(), workout.isFavorite());
        } else {
            // If database update failed, revert the UI change
            workout.setFavorite(!newFavoriteStatus);
            updateFavoriteButtonText();
            
            logger.error("Failed to toggle favorite status for workout: {}", workout.getName());
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Failed to " + (newFavoriteStatus ? "add to" : "remove from") + " favorites. Please try again later.");
        }
    }
    
    private void updateFavoriteButtonText() {
        favoriteButton.setText(workout.isFavorite() ? "Unfavorite" : "Favorite");
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
