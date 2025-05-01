/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aigym.app.controllers;

import com.aigym.app.models.User;
import com.aigym.app.models.Workout;
import com.aigym.app.services.WorkoutService;
import com.aigym.app.utils.ApiClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class DashboardController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    private User currentUser;
    private WorkoutService workoutService = new WorkoutService();
    private ApiClient apiClient = ApiClient.getInstance();
    
    @FXML private Label welcomeLabel;
    @FXML private Label motivationalQuoteLabel;
    @FXML private Label quoteAuthorLabel;
    @FXML private VBox workoutRecommendationsContainer;
    @FXML private LineChart<String, Number> progressChart;
    @FXML private Button dashboardButton;
    @FXML private Button workoutsButton;
    @FXML private Button progressButton;
    @FXML private Button profileButton;
    @FXML private Button logoutButton;
    
    @FXML
    public void initialize() {
        // This method is automatically called after the FXML is loaded
    }
    
    public void initData(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getFirstName() + "!");
        
        loadMotivationalQuote();
        loadWorkoutRecommendations();
        loadProgressData();
    }
    
    @FXML
    private void handleDashboardButtonAction(ActionEvent event) {
        // Already on dashboard, no action needed
        setActiveButton(dashboardButton);
    }
    
    @FXML
    private void handleWorkoutsButtonAction(ActionEvent event) {
        try {
            setActiveButton(workoutsButton);
            loadView("/fxml/workouts.fxml", "Workouts - AI Gym Workout System");
        } catch (IOException e) {
            logger.error("Error loading workouts view", e);
            showErrorAlert("Could not load workouts page");
        }
    }
    
    @FXML
    private void handleProgressButtonAction(ActionEvent event) {
        try {
            setActiveButton(progressButton);
            loadView("/fxml/progress.fxml", "Progress - AI Gym Workout System");
        } catch (IOException e) {
            logger.error("Error loading progress view", e);
            showErrorAlert("Could not load progress page");
        }
    }
    
    @FXML
    private void handleProfileButtonAction(ActionEvent event) {
        try {
            setActiveButton(profileButton);
            
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
            showErrorAlert("Could not load profile page");
        }
    }
    
    @FXML
    private void handleLogoutButtonAction(ActionEvent event) {
        try {
            // Navigate back to login screen
            Parent loginView = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Scene loginScene = new Scene(loginView);
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.setScene(loginScene);
            currentStage.setTitle("Login - AI Gym Workout System");
            
            // Clear current user
            currentUser = null;
            
            logger.info("User logged out successfully");
        } catch (IOException e) {
            logger.error("Error navigating to login view", e);
            showErrorAlert("Could not log out. Please try again.");
        }
    }
    
    /**
     * Sets the current user and initializes the dashboard with their data
     * @param user The current user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + user.getFirstName() + "!");
        }
    }
    
    /**
     * Refreshes all dashboard data for the current user
     */
    public void refreshDashboard() {
        if (currentUser == null) {
            logger.warn("Cannot refresh dashboard: no current user");
            return;
        }
        
        // Reload all dashboard components
        loadMotivationalQuote();
        loadWorkoutRecommendations();
        loadProgressData();
        
        logger.info("Dashboard refreshed for user: {}", currentUser.getUsername());
    }
    
    private void loadView(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent view = loader.load();
        
        if (loader.getController() instanceof BaseController) {
            BaseController controller = (BaseController) loader.getController();
            controller.initData(currentUser);
        }
        
        Scene scene = new Scene(view);
        Stage currentStage = (Stage) dashboardButton.getScene().getWindow();
        currentStage.setScene(scene);
        currentStage.setTitle(title);
    }
    
    private void setActiveButton(Button activeButton) {
        // Remove active class from all buttons
        dashboardButton.getStyleClass().remove("active");
        workoutsButton.getStyleClass().remove("active");
        progressButton.getStyleClass().remove("active");
        profileButton.getStyleClass().remove("active");
        
        // Add active class to the clicked button
        if (!activeButton.getStyleClass().contains("active")) {
            activeButton.getStyleClass().add("active");
        }
    }
    
    private void showErrorAlert(String message) {
        // In a real implementation, you would show a proper alert dialog
        logger.error(message);
    }
    
    private void loadMotivationalQuote() {
        try {
            // Determine time of day (morning or evening)
            int hour = java.time.LocalTime.now().getHour();
            String timeOfDay = (hour >= 5 && hour < 12) ? "MORNING" : 
                              (hour >= 17 && hour <= 23) ? "EVENING" : "ANY";
            
            // Call the API to get a motivational quote
            JSONObject response = apiClient.get("/quote?timeOfDay=" + timeOfDay);
            
            if (response.has("quote") && response.has("author")) {
                motivationalQuoteLabel.setText(response.getString("quote"));
                quoteAuthorLabel.setText("- " + response.getString("author"));
            }
        } catch (Exception e) {
            logger.error("Error loading motivational quote", e);
            motivationalQuoteLabel.setText("Every day is a new opportunity to improve yourself.");
            quoteAuthorLabel.setText("- Anonymous");
        }
    }

    private void loadWorkoutRecommendations() {
        try {
            // Prepare request data
            JSONObject requestData = new JSONObject();
            requestData.put("userId", currentUser.getId());
            requestData.put("fitnessGoal", currentUser.getFitnessGoal());
            requestData.put("fitnessLevel", currentUser.getFitnessLevel());
            
            // Call the API to get workout recommendations
            JSONObject response = apiClient.post("/workout/recommend", requestData);
            
            // Process and display the workout recommendation
            Workout recommendedWorkout = workoutService.convertJsonToWorkout(response);
            displayWorkoutRecommendation(recommendedWorkout);
        } catch (Exception e) {
            logger.error("Error loading workout recommendations", e);
            // Display a fallback message or previously saved recommendations
        }
    }

    private void displayWorkoutRecommendation(Workout workout) {
        try {
            // Clear previous recommendations
            workoutRecommendationsContainer.getChildren().clear();
            
            // Load the workout card FXML for each workout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/workout_card.fxml"));
            Parent workoutCard = loader.load();
            
            // Get the controller and initialize it with the workout data
            WorkoutCardController cardController = loader.getController();
            cardController.setWorkout(workout);
            cardController.setCurrentUser(currentUser);  // Pass the current user to the card controller
            
            // Add the workout card to the container
            workoutRecommendationsContainer.getChildren().add(workoutCard);
        } catch (IOException e) {
            logger.error("Error displaying workout recommendation", e);
        }
    }

    private void loadProgressData() {
        //  load data from the database and populate the progress chart
        // For now, we'll just clear the chart
        progressChart.getData().clear();
        
        // In a real implementation, you would:
        // 1. Query the database for progress data
        // 2. Create series for different metrics (weight, reps, etc.)
        // 3. Add the series to the chart
    }
}
