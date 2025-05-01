/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aigym.app.controllers;

import com.aigym.app.models.User;
import com.aigym.app.models.WorkoutHistory;
import com.aigym.app.services.ProgressService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ProgressController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(ProgressController.class);
    private ProgressService progressService = new ProgressService();
    
    @FXML private Button dashboardButton;
    @FXML private Button workoutsButton;
    @FXML private Button progressButton;
    @FXML private Button profileButton;
    @FXML private Button logoutButton;
    
    @FXML private ComboBox<String> metricComboBox;
    @FXML private ComboBox<String> timePeriodComboBox;
    @FXML private LineChart<String, Number> progressChart;
    
    @FXML private Label totalWorkoutsLabel;
    @FXML private Label totalTimeLabel;
    @FXML private Label caloriesBurnedLabel;
    
    @FXML private TableView<WorkoutHistory> workoutHistoryTable;
    @FXML private TableColumn<WorkoutHistory, LocalDate> dateColumn;
    @FXML private TableColumn<WorkoutHistory, String> workoutNameColumn;
    @FXML private TableColumn<WorkoutHistory, Integer> durationColumn;
    @FXML private TableColumn<WorkoutHistory, Integer> caloriesColumn;
    @FXML private TableColumn<WorkoutHistory, String> statusColumn;
    
    @FXML
    public void initialize() {
        // Initialize combo boxes
        metricComboBox.setItems(FXCollections.observableArrayList(
                "Weight", "Body Fat %", "Muscle Mass", "Workout Duration", "Calories Burned"));
        metricComboBox.getSelectionModel().selectFirst();
        
        timePeriodComboBox.setItems(FXCollections.observableArrayList(
                "Last Week", "Last Month", "Last 3 Months", "Last Year", "All Time"));
        timePeriodComboBox.getSelectionModel().selectFirst();
        
        // Initialize table columns
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        workoutNameColumn.setCellValueFactory(new PropertyValueFactory<>("workoutName"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        caloriesColumn.setCellValueFactory(new PropertyValueFactory<>("caloriesBurned"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Format date column
        dateColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });
    }
    
    @Override
    public void initData(User user) {
        super.initData(user);
        loadProgressData();
        loadWorkoutHistory();
    }
    
    @FXML
    private void handleUpdateChartButtonAction(ActionEvent event) {
        loadProgressData();
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
        // Already on progress page, no action needed
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
    
    private void loadProgressData() {
        try {
            String selectedMetric = metricComboBox.getValue();
            String selectedTimePeriod = timePeriodComboBox.getValue();
            
            // Clear previous data
            progressChart.getData().clear();
            
            // Get progress data from service
            Map<String, Double> progressData = progressService.getUserProgressData(
                    currentUser.getId(), selectedMetric, selectedTimePeriod);
            
            if (progressData.isEmpty()) {
                // Show message when no data is available
                progressChart.setTitle("No data available for " + selectedMetric);
            } else {
                progressChart.setTitle(selectedMetric + " over time");
                
                // Create series for the chart
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName(selectedMetric);
                
                // Add data points to the series
                for (Map.Entry<String, Double> entry : progressData.entrySet()) {
                    series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                }
                
                // Add series to the chart
                progressChart.getData().add(series);
            }
            
            // Update summary statistics
            updateSummaryStatistics();
        } catch (Exception e) {
            logger.error("Error loading progress data: {}", e.getMessage());
            
            // Clear chart and show error message
            progressChart.getData().clear();
            progressChart.setTitle("Error loading progress data");
            
            // Set default values for summary statistics
            totalWorkoutsLabel.setText("0");
            totalTimeLabel.setText("0");
            caloriesBurnedLabel.setText("0");
            
            // Show a user-friendly error message
            showAlert(Alert.AlertType.ERROR, "Data Error", "Could not load progress data. Please try again later.");
        }
    }
    
    private void loadWorkoutHistory() {
        try {
            // Get workout history from service
            List<WorkoutHistory> workoutHistory = progressService.getUserWorkoutHistory(currentUser.getId());
            
            if (workoutHistory.isEmpty()) {
                // Show placeholder message in the table
                workoutHistoryTable.setPlaceholder(new Label("No workout history available"));
            }
            
            // Update table with workout history
            ObservableList<WorkoutHistory> data = FXCollections.observableArrayList(workoutHistory);
            workoutHistoryTable.setItems(data);
        } catch (Exception e) {
            logger.error("Error loading workout history: {}", e.getMessage());
            
            // Set empty data and show placeholder message
            workoutHistoryTable.setItems(FXCollections.observableArrayList());
            workoutHistoryTable.setPlaceholder(new Label("Could not load workout history"));
            
            // Show a user-friendly error message
            showAlert(Alert.AlertType.ERROR, "Data Error", "Could not load workout history. Please try again later.");
        }
    }
    
    private void updateSummaryStatistics() {
        try {
            // Get summary statistics from service
            int totalWorkouts = progressService.getUserTotalWorkouts(currentUser.getId());
            int totalMinutes = progressService.getUserTotalWorkoutTime(currentUser.getId());
            int totalCalories = progressService.getUserTotalCaloriesBurned(currentUser.getId());
            
            // Update labels
            totalWorkoutsLabel.setText(String.valueOf(totalWorkouts));
            totalTimeLabel.setText(formatTime(totalMinutes));
            caloriesBurnedLabel.setText(totalCalories + " kcal");
        } catch (Exception e) {
            logger.error("Error updating summary statistics", e);
        }
    }
    
    private String formatTime(int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        
        if (hours > 0) {
            return hours + " hr " + minutes + " min";
        } else {
            return minutes + " min";
        }
    }
    
    @Override
    protected Parent getRoot() {
        return dashboardButton.getParent().getParent();
    }
}
