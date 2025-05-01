/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aigym.app.controllers;

import com.aigym.app.models.Exercise;
import com.aigym.app.models.User;
import com.aigym.app.models.Workout;
import com.aigym.app.services.WorkoutService;
import com.aigym.app.utils.ApiClient;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkoutsController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(WorkoutsController.class);
    private WorkoutService workoutService = new WorkoutService();
    private ApiClient apiClient = ApiClient.getInstance();
    
    @FXML private Button dashboardButton;
    @FXML private Button workoutsButton;
    @FXML private Button progressButton;
    @FXML private Button profileButton;
    @FXML private Button logoutButton;
    @FXML private Button createWorkoutButton;
    @FXML private Button getAiRecommendationsButton;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private FlowPane workoutsContainer;
    
    private List<Workout> allWorkouts = new ArrayList<>();
    
    @FXML
    public void initialize() {
        // Initialize filter options
        filterComboBox.setItems(FXCollections.observableArrayList(
                "All Workouts", "My Workouts", "Favorites", "Strength", "Cardio", 
                "Flexibility", "Beginner", "Intermediate", "Advanced","GURU", "AI Generated"));
        filterComboBox.setValue("All Workouts");
        
        // Add listener for filter changes
        filterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterWorkouts(newValue);
        });
    }
    
    @Override
    public void initData(User user) {
        super.initData(user);
        loadWorkouts();
    }
    
    private void loadWorkouts() {
        try {
            // Get all workouts for the user
            allWorkouts = workoutService.getUserWorkouts(currentUser.getId());
            
            // Apply current filter
            filterWorkouts(filterComboBox.getValue());
        } catch (Exception e) {
            logger.error("Error loading workouts: {}", e.getMessage());
            allWorkouts = new ArrayList<>(); // Initialize with empty list on error
            
            // Display a user-friendly message in the workouts container
            workoutsContainer.getChildren().clear();
            Label errorLabel = new Label("Unable to load workouts. Please try again later.");
            errorLabel.getStyleClass().add("error-label");
            workoutsContainer.getChildren().add(errorLabel);
            
            // Show a less technical error message to the user
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load workouts: Database error while retrieving workouts");
        }
    }
    
    private void filterWorkouts(String filter) {
        workoutsContainer.getChildren().clear();
        
        List<Workout> filteredWorkouts = new ArrayList<>();
        
        switch (filter) {
            case "All Workouts":
                filteredWorkouts = allWorkouts;
                break;
            case "My Workouts":
                // Since we don't have a getUserId method, we'll need to filter another way
                // For now, let's assume all workouts belong to the current user
                filteredWorkouts = allWorkouts;
                break;
            case "Favorites":
                filteredWorkouts = workoutService.getUserFavoriteWorkouts(currentUser.getId());
                break;
            case "Strength":
            case "Cardio":
            case "Flexibility":
                String category = filter.toLowerCase();
                filteredWorkouts = allWorkouts.stream()
                        .filter(w -> category.equals(w.getCategory().toLowerCase()))
                        .toList();
                break;
            case "Beginner":
            case "Intermediate":
            case "Advanced":
                String difficulty = filter.toLowerCase();
                filteredWorkouts = allWorkouts.stream()
                        .filter(w -> difficulty.equals(w.getDifficulty().toLowerCase()))
                        .toList();
                break;
            case "AI Generated":
                filteredWorkouts = allWorkouts.stream()
                        .filter(Workout::isAiGenerated)
                        .toList();
                break;
        }
        
        displayWorkouts(filteredWorkouts);
    }
    
    private void displayWorkouts(List<Workout> workouts) {
        workoutsContainer.getChildren().clear();
        
        if (workouts.isEmpty()) {
            Label noWorkoutsLabel = new Label("No workouts found");
            noWorkoutsLabel.getStyleClass().add("no-data-label");
            workoutsContainer.getChildren().add(noWorkoutsLabel);
            return;
        }
        
        for (Workout workout : workouts) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/workout_card.fxml"));
                VBox workoutCard = loader.load();
                
                WorkoutCardController controller = loader.getController();
                controller.setWorkout(workout);
                controller.setCurrentUser(currentUser); // Pass the current user to the controller
                
                workoutCard.getStyleClass().add("workout-card");
                FlowPane.setMargin(workoutCard, new Insets(10));
                
                workoutsContainer.getChildren().add(workoutCard);
            } catch (IOException e) {
                logger.error("Error loading workout card: {}", e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleCreateWorkoutButtonAction(ActionEvent event) {
        try {
            // Create a dialog for creating a new workout
            Dialog<Workout> dialog = new Dialog<>();
            dialog.setTitle("Create New Workout");
            dialog.setHeaderText("Enter workout details");
            
            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            // Create the form grid
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            TextField nameField = new TextField();
            nameField.setPromptText("Workout Name");
            
            TextArea descriptionArea = new TextArea();
            descriptionArea.setPromptText("Description");
            
            TextField durationField = new TextField();
            durationField.setPromptText("Duration (minutes)");
            
            ComboBox<String> difficultyComboBox = new ComboBox<>();
            difficultyComboBox.setItems(FXCollections.observableArrayList(
                    "Beginner", "Intermediate", "Advanced"));
            difficultyComboBox.setPromptText("Difficulty");
            
            ComboBox<String> categoryComboBox = new ComboBox<>();
            categoryComboBox.setItems(FXCollections.observableArrayList(
                    "Strength", "Cardio", "Flexibility", "Balance", "HIIT"));
            categoryComboBox.setPromptText("Category");
            
            ComboBox<String> targetMuscleGroupComboBox = new ComboBox<>();
            targetMuscleGroupComboBox.setItems(FXCollections.observableArrayList(
                    "Full Body", "Upper Body", "Lower Body", "Core", "Back", "Chest", 
                    "Arms", "Shoulders", "Legs", "Glutes"));
            targetMuscleGroupComboBox.setPromptText("Target Muscle Group");
            
            // Add fields to grid
            grid.add(new Label("Name:"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(new Label("Description:"), 0, 1);
            grid.add(descriptionArea, 1, 1);
            grid.add(new Label("Duration (minutes):"), 0, 2);
            grid.add(durationField, 1, 2);
            grid.add(new Label("Difficulty:"), 0, 3);
            grid.add(difficultyComboBox, 1, 3);
            grid.add(new Label("Category:"), 0, 4);
            grid.add(categoryComboBox, 1, 4);
            grid.add(new Label("Target Muscle Group:"), 0, 5);
            grid.add(targetMuscleGroupComboBox, 1, 5);
            
            dialog.getDialogPane().setContent(grid);
            
            // Request focus on the name field by default
            nameField.requestFocus();
            
            // Convert the result to a workout when the save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        // Validate inputs
                        if (nameField.getText().isEmpty()) {
                            showAlert(Alert.AlertType.ERROR, "Validation Error", "Workout name is required");
                            return null;
                        }
                        
                        if (durationField.getText().isEmpty() || !durationField.getText().matches("\\d+")) {
                            showAlert(Alert.AlertType.ERROR, "Validation Error", "Duration must be a valid number");
                            return null;
                        }
                        
                        if (difficultyComboBox.getValue() == null) {
                            showAlert(Alert.AlertType.ERROR, "Validation Error", "Difficulty is required");
                            return null;
                        }
                        
                        if (categoryComboBox.getValue() == null) {
                            showAlert(Alert.AlertType.ERROR, "Validation Error", "Category is required");
                            return null;
                        }
                        
                        // Create workout object
                        Workout workout = new Workout();
                        workout.setName(nameField.getText());
                        workout.setDescription(descriptionArea.getText());
                        workout.setDuration(Integer.parseInt(durationField.getText()));
                        workout.setDifficulty(difficultyComboBox.getValue());
                        workout.setCategory(categoryComboBox.getValue());
                        workout.setTargetMuscleGroup(targetMuscleGroupComboBox.getValue());
                        workout.setCaloriesBurn(calculateEstimatedCalories(
                                Integer.parseInt(durationField.getText()), 
                                difficultyComboBox.getValue()));
                        workout.setAiGenerated(false);
                        
                        return workout;
                    } catch (Exception e) {
                        logger.error("Error creating workout", e);
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to create workout: " + e.getMessage());
                        return null;
                    }
                }
                return null;
            });
            
            Optional<Workout> result = dialog.showAndWait();
            
            result.ifPresent(workout -> {
                try {
                    // Save workout to database
                    Workout savedWorkout = workoutService.createWorkout(workout, currentUser.getId());
                    
                    if (savedWorkout != null) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Workout created successfully");
                        
                        // Reload workouts
                        loadWorkouts();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to create workout");
                    }
                } catch (Exception e) {
                    logger.error("Error saving workout", e);
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to save workout: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            logger.error("Error creating workout dialog", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open workout creation dialog: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleGetAiRecommendationsButtonAction(ActionEvent event) {
        try {
            // Create a dialog for AI workout recommendations
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("AI Workout Recommendations");
            dialog.setHeaderText("Describe what kind of workout you're looking for");
            
            // Set the button types
            ButtonType generateButtonType = new ButtonType("Generate", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(generateButtonType, ButtonType.CANCEL);
            
            // Create the form grid
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            TextArea promptArea = new TextArea();
            promptArea.setPromptText("E.g., 'A 30-minute HIIT workout for beginners focusing on upper body'");
            promptArea.setPrefRowCount(5);
            
            // Add fields to grid
            grid.add(new Label("Your Request:"), 0, 0);
            grid.add(promptArea, 0, 1);
            
            dialog.getDialogPane().setContent(grid);
            
            // Request focus on the prompt area by default
            promptArea.requestFocus();
            
            // Convert the result when the generate button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == generateButtonType) {
                    return promptArea.getText();
                }
                return null;
            });
            
            Optional<String> result = dialog.showAndWait();
            
            result.ifPresent(prompt -> {
                if (prompt.trim().isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a description for the workout");
                    return;
                }
                
                // Show loading indicator
                ProgressIndicator progressIndicator = new ProgressIndicator();
                Stage loadingStage = createLoadingStage(progressIndicator, "Generating AI Workout...");
                loadingStage.show();
                
                // Create a new thread to handle the API call
                new Thread(() -> {
                    try {
                        // Call API to get workout recommendation
                        JSONObject requestBody = new JSONObject();
                        requestBody.put("prompt", prompt);
                        requestBody.put("userId", currentUser.getId());
                        requestBody.put("user_id", currentUser.getId());
                        
                        // Handle potentially null fitness level and goal
                        String fitnessLevel = currentUser.getFitnessLevel();
                        requestBody.put("fitness_level", fitnessLevel != null ? fitnessLevel : "intermediate");
                        
                        String fitnessGoal = currentUser.getFitnessGoal();
                        requestBody.put("fitness_goal", fitnessGoal != null ? fitnessGoal : "general_fitness");
                        
                        logger.info("Sending workout generation request: {}", requestBody.toString());
                        logger.info("API URL: {}/api/workouts/generate", ApiClient.API_BASE_URL);
                        
                        try {
                            JSONObject response = apiClient.post("/workouts/generate", requestBody);
                            
                            if (response != null && !response.has("error")) {
                                Workout workout = workoutService.convertJsonToWorkout(response);
                                workout.setAiGenerated(true);
                                
                                // Save workout to database on JavaFX thread
                                javafx.application.Platform.runLater(() -> {
                                    try {
                                        Workout savedWorkout = workoutService.createWorkout(workout, currentUser.getId());
                                        
                                        if (savedWorkout != null) {
                                            showAlert(Alert.AlertType.INFORMATION, "Success", 
                                                    "AI workout generated and saved successfully");
                                            
                                            // Reload workouts
                                            loadWorkouts();
                                        } else {
                                            showAlert(Alert.AlertType.ERROR, "Error", 
                                                    "Failed to save AI generated workout");
                                        }
                                    } catch (Exception e) {
                                        logger.error("Error saving AI workout", e);
                                        showAlert(Alert.AlertType.ERROR, "Error", 
                                                "Failed to save AI workout: " + e.getMessage());
                                    } finally {
                                        loadingStage.close();
                                    }
                                });
                            } else {
                                javafx.application.Platform.runLater(() -> {
                                    loadingStage.close();
                                    showAlert(Alert.AlertType.ERROR, "Error", 
                                            "Failed to generate workout. Please try again with a different description.");
                                });
                            }
                        } catch (Exception e) {
                            logger.error("Error generating AI workout", e);
                            javafx.application.Platform.runLater(() -> {
                                loadingStage.close();
                                showAlert(Alert.AlertType.ERROR, "Error", 
                                        "Failed to generate AI workout: " + e.getMessage());
                            });
                        }
                    } catch (Exception e) {
                        logger.error("Error generating AI workout", e);
                        javafx.application.Platform.runLater(() -> {
                            loadingStage.close();
                            showAlert(Alert.AlertType.ERROR, "Error", 
                                    "Failed to generate AI workout: " + e.getMessage());
                        });
                    }
                }).start();
            });
        } catch (Exception e) {
            logger.error("Error creating AI recommendation dialog", e);
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Failed to open AI recommendation dialog: " + e.getMessage());
        }
    }
    
    private Stage createLoadingStage(ProgressIndicator progressIndicator, String message) {
        VBox root = new VBox(10);
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(new Label(message), progressIndicator);
        
        Scene scene = new Scene(root, 300, 150);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Loading");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        
        return stage;
    }
    
    private int calculateEstimatedCalories(int duration, String difficulty) {
        // Simple formula to estimate calories burned based on duration and difficulty
        int baseRate;
        
        switch (difficulty.toLowerCase()) {
            case "beginner":
                baseRate = 5;
                break;
            case "intermediate":
                baseRate = 7;
                break;
            case "advanced":
                baseRate = 10;
                break;
            default:
                baseRate = 6;
        }
        
        return duration * baseRate;
    }
    
    @FXML
    private void handleWorkoutsButtonAction(ActionEvent event) {
        try {
            // Reload the workouts page to refresh the data
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/workouts.fxml"));
            Parent workoutsView = loader.load();
            
            WorkoutsController workoutsController = loader.getController();
            workoutsController.initData(currentUser);
            
            Scene workoutsScene = new Scene(workoutsView);
            Stage currentStage = (Stage) workoutsButton.getScene().getWindow();
            currentStage.setScene(workoutsScene);
            currentStage.setTitle("Workouts - AI Gym Workout System");
        } catch (IOException e) {
            logger.error("Error reloading workouts view", e);
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not reload workouts page");
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            Parent profileView = loader.load();
            
            ProfileController profileController = loader.getController();
            profileController.initData(currentUser);
            
            Scene profileScene = new Scene(profileView);
            Stage currentStage = (Stage) profileButton.getScene().getWindow();
            currentStage.setScene(profileScene);
            currentStage.setTitle("Profile - AI Gym Workout System");
        } catch (IOException e) {
            logger.error("Error loading profile view", e);
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load profile page");
        }
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
