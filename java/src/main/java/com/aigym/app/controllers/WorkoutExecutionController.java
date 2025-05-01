/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aigym.app.controllers;

import com.aigym.app.models.Exercise;
import com.aigym.app.models.User;
import com.aigym.app.models.Workout;
import com.aigym.app.models.WorkoutHistory;
import com.aigym.app.services.ProgressService;
import com.aigym.app.services.UserService;
import com.aigym.app.services.WorkoutService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class WorkoutExecutionController {
    private static final Logger logger = LoggerFactory.getLogger(WorkoutExecutionController.class);
    
    private Workout workout;
    private User currentUser;
    private WorkoutService workoutService = new WorkoutService();
    private UserService userService = new UserService();
    private ProgressService progressService = new ProgressService();
    
    private List<Exercise> exercises;
    private int currentExerciseIndex = 0;
    private IntegerProperty secondsRemaining = new SimpleIntegerProperty();
    private StringProperty timeDisplay = new SimpleStringProperty();
    private Timeline timer;
    private int totalCaloriesBurned = 0;
    private LocalDateTime startTime;
    
    @FXML private Label workoutNameLabel;
    @FXML private Label exerciseNameLabel;
    @FXML private Label exerciseDescriptionLabel;
    @FXML private Label setsRepsLabel;
    @FXML private Label timerLabel;
    @FXML private Label caloriesLabel;
    @FXML private ProgressBar workoutProgressBar;
    @FXML private Button nextExerciseButton;
    @FXML private Button pauseResumeButton;
    @FXML private Button finishWorkoutButton;
    
    @FXML
    public void initialize() {
        logger.info("Initializing WorkoutExecutionController");
        
        // Bind timer display to the timeDisplay property
        timerLabel.textProperty().bind(timeDisplay);
        
        // Initialize with disabled buttons until workout is started
        nextExerciseButton.setDisable(true);
        pauseResumeButton.setDisable(true);
        finishWorkoutButton.setDisable(true);
    }
    
    public void setWorkout(Workout workout) {
        this.workout = workout;
        this.exercises = workout.getExercises();
        
        // Update UI with workout details
        workoutNameLabel.setText(workout.getName());
        
        // Get current user
        this.currentUser = userService.getCurrentUser();
        if (this.currentUser == null) {
            logger.warn("Current user is null in WorkoutExecutionController");
        } else {
            logger.info("Current user set: {}", currentUser.getUsername());
        }
        
        logger.info("Workout set: {} with {} exercises", workout.getName(), 
                exercises != null ? exercises.size() : 0);
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        logger.info("Current user set manually: {}", user.getUsername());
    }
    
    public void startWorkout() {
        if (exercises == null || exercises.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "This workout has no exercises.");
            return;
        }
        
        startTime = LocalDateTime.now();
        
        // Enable buttons
        nextExerciseButton.setDisable(false);
        pauseResumeButton.setDisable(false);
        finishWorkoutButton.setDisable(false);
        
        // Display first exercise
        currentExerciseIndex = 0;
        displayCurrentExercise();
        
        // Start timer
        startTimer();
        
        logger.info("Workout started: {}", workout.getName());
    }
    
    private void displayCurrentExercise() {
        if (currentExerciseIndex < exercises.size()) {
            Exercise exercise = exercises.get(currentExerciseIndex);
            exerciseNameLabel.setText(exercise.getName());
            exerciseDescriptionLabel.setText(exercise.getDescription());
            setsRepsLabel.setText(String.format("%d sets x %d reps", exercise.getSets(), exercise.getReps()));
            
            // Update progress bar
            double progress = (double) (currentExerciseIndex) / exercises.size();
            workoutProgressBar.setProgress(progress);
            
            logger.info("Displaying exercise {}/{}: {}", 
                    currentExerciseIndex + 1, exercises.size(), exercise.getName());
        }
    }
    
    private void startTimer() {
        // Initialize with 60 seconds for each exercise
        secondsRemaining.set(60);
        updateTimeDisplay();
        
        timer = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    secondsRemaining.set(secondsRemaining.get() - 1);
                    updateTimeDisplay();
                    
                    // Calculate calories burned (simplified calculation)
                    totalCaloriesBurned += 1; // 1 calorie per second as a simple example
                    caloriesLabel.setText("Calories: " + totalCaloriesBurned);
                    
                    if (secondsRemaining.get() <= 0) {
                        // Auto-advance to next exercise when timer reaches zero
                        handleNextExerciseButtonAction();
                    }
                })
        );
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
        
        pauseResumeButton.setText("Pause");
    }
    
    private void updateTimeDisplay() {
        int minutes = secondsRemaining.get() / 60;
        int seconds = secondsRemaining.get() % 60;
        timeDisplay.set(String.format("%02d:%02d", minutes, seconds));
    }
    
    @FXML
    private void handleNextExerciseButtonAction() {
        currentExerciseIndex++;
        
        if (currentExerciseIndex < exercises.size()) {
            // Reset timer for next exercise
            secondsRemaining.set(60);
            displayCurrentExercise();
        } else {
            // End of workout
            handleFinishWorkoutButtonAction();
        }
    }
    
    @FXML
    private void handlePauseResumeButtonAction() {
        if (timer.getStatus() == Timeline.Status.RUNNING) {
            timer.pause();
            pauseResumeButton.setText("Resume");
        } else {
            timer.play();
            pauseResumeButton.setText("Pause");
        }
    }
    
    @FXML
    private void handleFinishWorkoutButtonAction() {
        // Stop the timer
        if (timer != null) {
            timer.stop();
        }
        
        // Calculate total duration in minutes
        int durationMinutes = (int) java.time.Duration.between(startTime, LocalDateTime.now()).toMinutes();
        if (durationMinutes < 1) durationMinutes = 1; // Minimum 1 minute
        
        // Save workout history
        try {
            if (currentUser == null) {
                logger.error("Cannot save workout history: current user is null");
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Could not save your workout progress. User information is missing.");
                return;
            }
            
            // If the workout doesn't have a valid ID (e.g., it's a recommended workout from the API),
            // save it to the database first
            if (workout.getId() <= 0) {
                try {
                    logger.info("Saving workout to database before recording history: {}", workout.getName());
                    workout = workoutService.createWorkout(workout, currentUser.getId());
                    
                    if (workout.getId() <= 0) {
                        logger.error("Failed to save workout to database: {}", workout.getName());
                        showAlert(Alert.AlertType.ERROR, "Error", 
                                "Failed to save workout to database. Your progress will not be recorded.");
                        return;
                    }
                    
                    logger.info("Successfully saved workout to database with ID: {}", workout.getId());
                } catch (Exception e) {
                    logger.error("Error saving workout to database: {}", e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Error", 
                            "Failed to save workout to database. Your progress will not be recorded.");
                    return;
                }
            }
            
            WorkoutHistory history = new WorkoutHistory();
            history.setUserId(currentUser.getId());
            history.setWorkoutId(workout.getId());
            history.setDurationMinutes(durationMinutes);
            history.setCaloriesBurned(totalCaloriesBurned);
            history.setDate(LocalDate.now());
            
            boolean saved = progressService.saveWorkoutHistory(history);
            
            if (saved) {
                logger.info("Workout completed and saved: {} - Duration: {} minutes, Calories: {}", 
                        workout.getName(), durationMinutes, totalCaloriesBurned);
                
                // Show completion alert
                showAlert(Alert.AlertType.INFORMATION, "Workout Complete", 
                        "Congratulations! You've completed the workout.\n" +
                        "Duration: " + durationMinutes + " minutes\n" +
                        "Calories burned: " + totalCaloriesBurned);
                
                // Navigate back to dashboard
                navigateToDashboard();
            } else {
                logger.error("Failed to save workout history");
                showAlert(Alert.AlertType.WARNING, "Warning", 
                        "Your workout was completed, but we couldn't save your progress.");
            }
        } catch (Exception e) {
            logger.error("Error saving workout history: {}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Could not save your workout progress. Please try again.");
        }
    }
    
    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent dashboardView = loader.load();
            
            DashboardController dashboardController = loader.getController();
            dashboardController.setCurrentUser(currentUser);
            dashboardController.refreshDashboard();
            
            Scene dashboardScene = new Scene(dashboardView);
            Stage currentStage = (Stage) finishWorkoutButton.getScene().getWindow();
            currentStage.setScene(dashboardScene);
            currentStage.setTitle("Dashboard - AI Gym Workout System");
        } catch (IOException e) {
            logger.error("Error navigating to dashboard: {}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Could not return to dashboard. Please restart the application.");
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}