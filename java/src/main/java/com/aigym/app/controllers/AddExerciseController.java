/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aigym.app.controllers;

import com.aigym.app.models.Exercise;
import com.aigym.app.models.Workout;
import com.aigym.app.services.WorkoutService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddExerciseController {
    private static final Logger logger = LoggerFactory.getLogger(AddExerciseController.class);
    
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> muscleGroupComboBox;
    @FXML private ComboBox<String> equipmentComboBox;
    @FXML private Spinner<Integer> setsSpinner;
    @FXML private Spinner<Integer> repsSpinner;
    @FXML private Spinner<Integer> durationSpinner;
    @FXML private TextField videoUrlField;
    @FXML private TextField imageUrlField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    private Workout currentWorkout;
    private WorkoutService workoutService = new WorkoutService();
    private WorkoutDetailsController parentController;
    
    @FXML
    public void initialize() {
        // Initialize muscle group options
        muscleGroupComboBox.setItems(FXCollections.observableArrayList(
                "chest", "back", "shoulders", "arms", "legs", "core", "full_body", "cardio"
        ));
        
        // Initialize equipment options
        equipmentComboBox.setItems(FXCollections.observableArrayList(
                "bodyweight", "dumbbell", "barbell", "kettlebell", "resistance band", "machine", "other"
        ));
        
        // Initialize spinners
        SpinnerValueFactory<Integer> setsValueFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 3);
        setsSpinner.setValueFactory(setsValueFactory);
        
        SpinnerValueFactory<Integer> repsValueFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 12);
        repsSpinner.setValueFactory(repsValueFactory);
        
        SpinnerValueFactory<Integer> durationValueFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 300, 0, 5);
        durationSpinner.setValueFactory(durationValueFactory);
    }
    
    public void setWorkout(Workout workout) {
        this.currentWorkout = workout;
    }
    
    public void setParentController(WorkoutDetailsController controller) {
        this.parentController = controller;
    }
    
    @FXML
    private void handleSaveButtonAction() {
        if (!validateInput()) {
            return;
        }
        
        try {
            // Create a new exercise from form data
            Exercise exercise = new Exercise();
            exercise.setName(nameField.getText().trim());
            exercise.setDescription(descriptionField.getText().trim());
            exercise.setMuscleGroup(muscleGroupComboBox.getValue());
            exercise.setEquipment(equipmentComboBox.getValue());
            exercise.setSets(setsSpinner.getValue());
            exercise.setReps(repsSpinner.getValue());
            exercise.setDuration(durationSpinner.getValue());
            exercise.setVideoUrl(videoUrlField.getText().trim());
            exercise.setImageUrl(imageUrlField.getText().trim());
            
            // Add exercise to workout
            boolean success = workoutService.addExerciseToExistingWorkout(currentWorkout.getId(), exercise);
            
            if (success) {
                // Refresh the parent controller to show the new exercise
                if (parentController != null) {
                    parentController.refreshExerciseList();
                }
                
                // Close the form
                closeForm();
                
                logger.info("Exercise '{}' added successfully to workout '{}'", 
                        exercise.getName(), currentWorkout.getName());
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Could not add exercise to workout. Please try again.");
            }
        } catch (Exception e) {
            logger.error("Error adding exercise: {}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "An error occurred while adding the exercise: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancelButtonAction() {
        closeForm();
    }
    
    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (nameField.getText().trim().isEmpty()) {
            errorMessage.append("Exercise name is required.\n");
        }
        
        if (descriptionField.getText().trim().isEmpty()) {
            errorMessage.append("Exercise description is required.\n");
        }
        
        if (muscleGroupComboBox.getValue() == null) {
            errorMessage.append("Please select a muscle group.\n");
        }
        
        if (equipmentComboBox.getValue() == null) {
            errorMessage.append("Please select equipment type.\n");
        }
        
        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", errorMessage.toString());
            return false;
        }
        
        return true;
    }
    
    private void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}