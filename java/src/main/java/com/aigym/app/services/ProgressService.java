/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aigym.app.services;

import com.aigym.app.models.WorkoutHistory;
import com.aigym.app.utils.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProgressService {
    private static final Logger logger = LoggerFactory.getLogger(ProgressService.class);
    private final DatabaseConnection dbConnection;
    
    public ProgressService() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    public Map<String, Double> getUserProgressData(int userId, String metric, String timePeriod) {
        String query;
        String dateFormat;
        LocalDate startDate = getStartDateForTimePeriod(timePeriod);
        
        // Determine the query based on the metric
        switch (metric) {
            case "Weight":
                query = "SELECT date, weight FROM user_metrics WHERE user_id = ? AND date >= ? ORDER BY date";
                break;
            case "Body Fat %":
                query = "SELECT date, body_fat_percentage FROM user_metrics WHERE user_id = ? AND date >= ? ORDER BY date";
                break;
            case "Muscle Mass":
                query = "SELECT date, muscle_mass FROM user_metrics WHERE user_id = ? AND date >= ? ORDER BY date";
                break;
            case "Workout Duration":
                query = "SELECT date, SUM(duration_minutes) as value FROM workout_history WHERE user_id = ? AND date >= ? GROUP BY date ORDER BY date";
                break;
            case "Calories Burned":
                query = "SELECT date, SUM(calories_burned) as value FROM workout_history WHERE user_id = ? AND date >= ? GROUP BY date ORDER BY date";
                break;
            default:
                logger.warn("Invalid metric requested: {}", metric);
                return new LinkedHashMap<>();
        }
        
        // Determine date format based on time period
        if ("Last Week".equals(timePeriod)) {
            dateFormat = "EEE"; // Day of week (e.g., Mon)
        } else if ("Last Month".equals(timePeriod)) {
            dateFormat = "dd MMM"; // Day and month (e.g., 15 Jan)
        } else {
            dateFormat = "MMM yyyy"; // Month and year (e.g., Jan 2023)
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        Map<String, Double> progressData = new LinkedHashMap<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            stmt.setDate(2, java.sql.Date.valueOf(startDate));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                try {
                    LocalDate date = rs.getDate("date").toLocalDate();
                    String formattedDate = date.format(formatter);
                    
                    double value;
                    if ("Weight".equals(metric) || "Body Fat %".equals(metric) || "Muscle Mass".equals(metric)) {
                        value = rs.getDouble(2);
                    } else {
                        value = rs.getDouble("value");
                    }
                    
                    progressData.put(formattedDate, value);
                } catch (SQLException e) {
                    logger.error("Error processing progress data row: {}", e.getMessage());
                }
            }
            
            return progressData;
        } catch (SQLException e) {
            logger.error("Error retrieving user progress data: {}", e.getMessage());
            // Return an empty map instead of throwing an exception
            return new LinkedHashMap<>();
        }
    }
    
    public List<WorkoutHistory> getUserWorkoutHistory(int userId) {
        List<WorkoutHistory> history = new ArrayList<>();
        
        // Try different query variations to accommodate different database schemas
        String[] queries = {
            // Try with join and workout_name from workouts table
            "SELECT h.*, w.name as workout_name FROM workout_history h " +
            "JOIN workouts w ON h.workout_id = w.id " +
            "WHERE h.user_id = ? ORDER BY h.created_at DESC, h.id DESC",
            
            // Try with join but without created_at ordering
            "SELECT h.*, w.name as workout_name FROM workout_history h " +
            "JOIN workouts w ON h.workout_id = w.id " +
            "WHERE h.user_id = ? ORDER BY h.id DESC",
            
            // Try without join, assuming workout_name is in workout_history
            "SELECT * FROM workout_history WHERE user_id = ? ORDER BY created_at DESC, id DESC",
            
            // Try without join and without created_at ordering
            "SELECT * FROM workout_history WHERE user_id = ? ORDER BY id DESC",
            
            // Try with join and date ordering instead of created_at
            "SELECT h.*, w.name as workout_name FROM workout_history h " +
            "JOIN workouts w ON h.workout_id = w.id " +
            "WHERE h.user_id = ? ORDER BY h.date DESC, h.id DESC",
            
            // Try without join and with date ordering
            "SELECT * FROM workout_history WHERE user_id = ? ORDER BY date DESC, id DESC",
            
            // Try without any ordering
            "SELECT * FROM workout_history WHERE user_id = ?"
        };
        
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            
            // Try each query until one works
            for (String query : queries) {
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, userId);
                    ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        try {
                            WorkoutHistory entry = mapResultSetToWorkoutHistory(rs);
                            history.add(entry);
                        } catch (SQLException e) {
                            // Log error but continue processing other rows
                            logger.error("Error mapping workout history row: {}", e.getMessage());
                        }
                    }
                    
                    // If we got here without exception and got some data, break out of the loop
                    if (!history.isEmpty()) {
                        logger.info("Successfully retrieved {} workout history entries using query: {}", 
                                history.size(), query);
                        break;
                    }
                } catch (SQLException e) {
                    // Log the error and try the next query
                    logger.warn("Query failed, trying alternative: {}", e.getMessage());
                }
            }
            
            return history;
        } catch (SQLException e) {
            logger.error("Error retrieving user workout history: {}", e.getMessage());
            return history; // Return empty list instead of throwing exception
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error closing connection: {}", e.getMessage());
                }
            }
        }
    }
    
    public int getUserTotalWorkouts(int userId) {
        // First try with status column
        String query = "SELECT COUNT(*) FROM workout_history WHERE user_id = ? AND status = 'completed'";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            try {
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                // If the query fails, it might be because the status column doesn't exist
                // Try again without the status condition
                logger.warn("Error executing query with status column, trying without status: {}", e.getMessage());
                String fallbackQuery = "SELECT COUNT(*) FROM workout_history WHERE user_id = ?";
                
                try (PreparedStatement fallbackStmt = conn.prepareStatement(fallbackQuery)) {
                    fallbackStmt.setInt(1, userId);
                    ResultSet fallbackRs = fallbackStmt.executeQuery();
                    
                    if (fallbackRs.next()) {
                        return fallbackRs.getInt(1);
                    }
                }
            }
            
            return 0;
        } catch (SQLException e) {
            logger.error("Error retrieving user total workouts", e);
            return 0; // Return 0 instead of throwing an exception
        }
    }
    
    public int getUserTotalCaloriesBurned(int userId) {
        // First try with status column
        String query = "SELECT SUM(calories_burned) FROM workout_history WHERE user_id = ? AND status = 'completed'";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            try {
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                // If the query fails, it might be because the status column doesn't exist
                // Try again without the status condition
                logger.warn("Error executing query with status column, trying without status: {}", e.getMessage());
                String fallbackQuery = "SELECT SUM(calories_burned) FROM workout_history WHERE user_id = ?";
                
                try (PreparedStatement fallbackStmt = conn.prepareStatement(fallbackQuery)) {
                    fallbackStmt.setInt(1, userId);
                    ResultSet fallbackRs = fallbackStmt.executeQuery();
                    
                    if (fallbackRs.next()) {
                        return fallbackRs.getInt(1);
                    }
                }
            }
            
            return 0;
        } catch (SQLException e) {
            logger.error("Error retrieving user total calories burned", e);
            return 0; // Return 0 instead of throwing an exception
        }
    }
    
    public int getUserTotalMinutesExercised(int userId) {
        // First try with status column
        String query = "SELECT SUM(duration_minutes) FROM workout_history WHERE user_id = ? AND status = 'completed'";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            try {
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                // If the query fails, it might be because the status column doesn't exist
                // Try again without the status condition
                logger.warn("Error executing query with status column, trying without status: {}", e.getMessage());
                String fallbackQuery = "SELECT SUM(duration_minutes) FROM workout_history WHERE user_id = ?";
                
                try (PreparedStatement fallbackStmt = conn.prepareStatement(fallbackQuery)) {
                    fallbackStmt.setInt(1, userId);
                    ResultSet fallbackRs = fallbackStmt.executeQuery();
                    
                    if (fallbackRs.next()) {
                        return fallbackRs.getInt(1);
                    }
                }
            }
            
            return 0;
        } catch (SQLException e) {
            logger.error("Error retrieving user total minutes exercised", e);
            return 0; // Return 0 instead of throwing an exception
        }
    }
    
    public int getUserTotalWorkoutTime(int userId) {
        // First try with status column
        String query = "SELECT SUM(duration_minutes) FROM workout_history WHERE user_id = ? AND status = 'completed'";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            try {
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                // If the query fails, it might be because the status column doesn't exist
                // Try again without the status condition
                logger.warn("Error executing query with status column, trying without status: {}", e.getMessage());
                String fallbackQuery = "SELECT SUM(duration_minutes) FROM workout_history WHERE user_id = ?";
                
                try (PreparedStatement fallbackStmt = conn.prepareStatement(fallbackQuery)) {
                    fallbackStmt.setInt(1, userId);
                    ResultSet fallbackRs = fallbackStmt.executeQuery();
                    
                    if (fallbackRs.next()) {
                        return fallbackRs.getInt(1);
                    }
                }
            }
            
            return 0;
        } catch (SQLException e) {
            logger.error("Error retrieving user total workout time", e);
            return 0; // Return 0 instead of throwing an exception
        }
    }
    
    public void recordWorkoutCompletion(WorkoutHistory workoutHistory) {
        String query = "INSERT INTO workout_history (user_id, workout_id, workout_name, date, " +
                       "duration_minutes, calories_burned, status, notes, created_at) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, workoutHistory.getUserId());
            stmt.setInt(2, workoutHistory.getWorkoutId());
            stmt.setString(3, workoutHistory.getWorkoutName());
            stmt.setDate(4, java.sql.Date.valueOf(workoutHistory.getDate()));
            stmt.setInt(5, workoutHistory.getDuration());
            stmt.setInt(6, workoutHistory.getCaloriesBurned());
            stmt.setString(7, workoutHistory.getStatus());
            stmt.setString(8, workoutHistory.getNotes());
            
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                logger.error("Recording workout completion failed, no rows affected.");
                return;
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    workoutHistory.setId(generatedKeys.getInt(1));
                } else {
                    logger.error("Recording workout completion failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            logger.error("Error recording workout completion: {}", e.getMessage());
            // Log error instead of throwing an exception
        }
    }
    
    /**
     * Saves a workout history entry to the database
     * @param history The workout history entry to save
     * @return true if successful, false otherwise
     */
    public boolean saveWorkoutHistory(WorkoutHistory history) {
        String query = "INSERT INTO workout_history (user_id, workout_id, duration_minutes, calories_burned, completed_at) " +
                      "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, history.getUserId());
            stmt.setInt(2, history.getWorkoutId());
            stmt.setInt(3, history.getDurationMinutes());
            stmt.setInt(4, history.getCaloriesBurned());
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Successfully saved workout history for user {} and workout {}", 
                        history.getUserId(), history.getWorkoutId());
                return true;
            } else {
                logger.warn("No rows affected when saving workout history");
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error saving workout history: {}", e.getMessage());
            return false;
        }
    }
    
    private LocalDate getStartDateForTimePeriod(String timePeriod) {
        LocalDate now = LocalDate.now();
        
        switch (timePeriod) {
            case "Last Week":
                return now.minusWeeks(1);
            case "Last Month":
                return now.minusMonths(1);
            case "Last 3 Months":
                return now.minusMonths(3);
            case "Last 6 Months":
                return now.minusMonths(6);
            case "Last Year":
                return now.minusYears(1);
            default:
                throw new IllegalArgumentException("Invalid time period: " + timePeriod);
        }
    }
    
    private WorkoutHistory mapResultSetToWorkoutHistory(ResultSet rs) throws SQLException {
        WorkoutHistory history = new WorkoutHistory();
        history.setId(rs.getInt("id"));
        history.setUserId(rs.getInt("user_id"));
        history.setWorkoutId(rs.getInt("workout_id"));
        history.setWorkoutName(rs.getString("workout_name"));
        history.setDate(rs.getDate("date").toLocalDate());
        history.setDuration(rs.getInt("duration_minutes"));
        history.setCaloriesBurned(rs.getInt("calories_burned"));
        history.setStatus(rs.getString("status"));
        history.setNotes(rs.getString("notes"));
        

        
        return history;
    }
}
