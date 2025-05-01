/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aigym.app.controllers;

import com.aigym.app.models.Exercise;
import com.aigym.app.models.User;
import com.aigym.app.models.Workout;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.stage.Modality;

import java.io.IOException;

public class WorkoutDetailsController {
    private static final Logger logger = LoggerFactory.getLogger(WorkoutDetailsController.class);
    private Workout workout;
    private User currentUser;

    @FXML private Label workoutNameLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label difficultyLabel;
    @FXML private Label durationLabel;
    @FXML private Label targetMusclesLabel;
    @FXML private ListView<Exercise> exercisesListView;
    @FXML private Button startWorkoutButton;
    @FXML private Button addToFavoritesButton;
    @FXML private VBox exerciseDetailsContainer;
    @FXML private Button addExerciseButton;
    @FXML private Button backButton;

    @FXML
    public void initialize() {
        // Initialize controller
        logger.info("Initializing WorkoutDetailsController");
    }

    public void setWorkout(Workout workout) {
        this.workout = workout;

        // Update UI with workout details
        workoutNameLabel.setText(workout.getName());
        descriptionLabel.setText(workout.getDescription());
        difficultyLabel.setText("Difficulty: " + workout.getDifficulty());
        durationLabel.setText("Duration: " + workout.getDuration() + " minutes");
        targetMusclesLabel.setText("Target Muscles: " + workout.getTargetMuscleGroup());

        // Populate exercises list
        if (workout.getExercises() != null) {
            exercisesListView.getItems().addAll(workout.getExercises());
            exercisesListView.setCellFactory(param -> new ExerciseListCell());
        }
    }

    /**
     * Sets the current user for this controller.
     * 
     * @param user The current user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void handleStartWorkoutButtonAction() {
        logger.info("Starting workout execution: {}", workout.getName());

        try {
            // Create a new scene for workout execution
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/workout_execution.fxml"));
            Parent workoutExecutionView = loader.load();

            // Get the controller and pass the workout data
            WorkoutExecutionController executionController = loader.getController();
            executionController.setWorkout(workout);
            
            // Pass the current user to the execution controller
            if (currentUser != null) {
                executionController.setCurrentUser(currentUser);
                logger.info("Passed user data to WorkoutExecutionController: {}", currentUser.getUsername());
            } else {
                logger.warn("No user data available to pass to WorkoutExecutionController");
            }
            
            executionController.startWorkout();

            // Create a new scene and set it on the current stage
            Scene workoutExecutionScene = new Scene(workoutExecutionView);
            Stage currentStage = (Stage) startWorkoutButton.getScene().getWindow();
            currentStage.setScene(workoutExecutionScene);
            currentStage.setTitle("Executing: " + workout.getName() + " - AI Gym Workout System");

            logger.info("Navigated to workout execution for: {}", workout.getName());
        } catch (IOException e) {
            logger.error("Error starting workout execution: {}", e.getMessage());

            // Show an alert with the error
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not start workout");
            alert.setContentText("An error occurred while trying to start the workout. Please try again.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleAddToFavoritesButtonAction() {
        // Toggle favorite status
        workout.setFavorite(!workout.isFavorite());
        updateFavoriteButtonText();

        logger.info("Toggled favorite status for workout {}: {}", 
                workout.getName(), workout.isFavorite());

        // In a real implementation, this would update the database
    }

    private void updateFavoriteButtonText() {
        addToFavoritesButton.setText(workout.isFavorite() ? "Remove from Favorites" : "Add to Favorites");
    }

    @FXML
    private void handleBackButtonAction() {
        logger.info("Navigating back from workout details: {}", workout.getName());
        
        try {
            // Load the workouts view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/workouts.fxml"));
            Parent workoutsView = loader.load();
            
            // Get the controller and pass the current user
            WorkoutsController controller = loader.getController();
            if (currentUser != null) {
                controller.initData(currentUser);
                logger.info("Passed user data to WorkoutsController: {}", currentUser.getUsername());
            } else {
                logger.warn("No user data available to pass to WorkoutsController");
            }
            
            // Create a new scene and set it on the current stage
            Scene workoutsScene = new Scene(workoutsView);
            Stage currentStage = (Stage) backButton.getScene().getWindow();
            currentStage.setScene(workoutsScene);
            currentStage.setTitle("Workouts - AI Gym Workout System");
            
            logger.info("Successfully navigated back to workouts view");
        } catch (IOException e) {
            logger.error("Error navigating back to workouts view: {}", e.getMessage());
            
            // Show an alert with the error
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Navigation Error");
            alert.setContentText("Could not navigate back to the workouts view. Please try again.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleAddExerciseButtonAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_exercise_form.fxml"));
            Parent root = loader.load();

            AddExerciseController controller = loader.getController();
            controller.setWorkout(workout);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add Exercise - " + workout.getName());
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            logger.error("Error opening add exercise form", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not open the add exercise form. Please try again.");
            alert.showAndWait();
        }
    }

    /**
     * Refreshes the exercise list after adding a new exercise
     */
    public void refreshExerciseList() {
        // Clear the current list
        exercisesListView.getItems().clear();

        // Reload the workout to get the updated exercise list
        com.aigym.app.services.WorkoutService workoutService = new com.aigym.app.services.WorkoutService();
        workout = workoutService.getWorkoutById(workout.getId());

        // Refresh the exercise display in the UI
        if (workout.getExercises() != null) {
            exercisesListView.getItems().addAll(workout.getExercises());
        }

        logger.info("Exercise list refreshed for workout: {}", workout.getName());
    }

    /**
     * Custom list cell for displaying exercises
     */
    private class ExerciseListCell extends javafx.scene.control.ListCell<Exercise> {
        @Override
        protected void updateItem(Exercise exercise, boolean empty) {
            super.updateItem(exercise, empty);

            if (empty || exercise == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(exercise.getName() + " - " + exercise.getMuscleGroup());
            }
        }
    }
}