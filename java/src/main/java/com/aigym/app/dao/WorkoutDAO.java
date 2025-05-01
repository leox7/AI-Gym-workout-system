/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aigym.app.dao;

import com.aigym.app.models.Exercise;
import com.aigym.app.models.Workout;
import com.aigym.app.utils.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WorkoutDAO {
    private static final Logger logger = LoggerFactory.getLogger(WorkoutDAO.class);
    private final DatabaseConnection dbConnection;
    private final ExerciseDAO exerciseDAO;
    
    public WorkoutDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.exerciseDAO = new ExerciseDAO();
    }
    
    public Workout getWorkoutById(int workoutId) {
        String query = "SELECT * FROM workouts WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, workoutId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Workout workout = mapResultSetToWorkout(rs);
                
                // Load exercises for this workout
                List<Exercise> exercises = getExercisesForWorkout(workoutId, conn);
                workout.setExercises(exercises);
                
                return workout;
            }
            
            return null;
        } catch (SQLException e) {
            logger.error("Error getting workout by ID", e);
            throw new RuntimeException("Database error retrieving workout", e);
        }
    }
    
    public List<Workout> getWorkoutsForUser(int userId) {
        String query = "SELECT w.* FROM workouts w " +
                      "JOIN user_workouts uw ON w.id = uw.workout_id " +
                      "WHERE uw.user_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            List<Workout> workouts = new ArrayList<>();
            while (rs.next()) {
                Workout workout = mapResultSetToWorkout(rs);
                
                // Load exercises for this workout
                List<Exercise> exercises = getExercisesForWorkout(workout.getId(), conn);
                workout.setExercises(exercises);
                
                workouts.add(workout);
            }
            
            return workouts;
        } catch (SQLException e) {
            logger.error("Error getting workouts for user", e);
            throw new RuntimeException("Database error retrieving workouts", e);
        }
    }
    
    public int saveWorkout(Workout workout) {
        String query = "INSERT INTO workouts (name, description, target_muscle_group, difficulty, " +
                      "duration, category, is_ai_generated, creator_user_id, created_at, updated_at) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, workout.getName());
            stmt.setString(2, workout.getDescription());
            stmt.setString(3, workout.getTargetMuscleGroup());
            stmt.setString(4, workout.getDifficulty());
            stmt.setInt(5, workout.getDuration());
            stmt.setString(6, workout.getCategory());
            stmt.setBoolean(7, workout.isAiGenerated());
            stmt.setInt(8, workout.getCreatorUserId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating workout failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int workoutId = generatedKeys.getInt(1);
                    workout.setId(workoutId);
                    
                    // Save exercises for this workout
                    if (workout.getExercises() != null) {
                        for (Exercise exercise : workout.getExercises()) {
                            exerciseDAO.saveExercise(exercise, workoutId);
                        }
                    }
                    
                    return workoutId;
                } else {
                    throw new SQLException("Creating workout failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving workout", e);
            throw new RuntimeException("Database error saving workout", e);
        }
    }
    
    public void updateWorkout(Workout workout) {
        String query = "UPDATE workouts SET name = ?, description = ?, target_muscle_group = ?, " +
                      "difficulty = ?, duration = ?, category = ?, is_ai_generated = ? " +
                      "WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, workout.getName());
            stmt.setString(2, workout.getDescription());
            stmt.setString(3, workout.getTargetMuscleGroup());
            stmt.setString(4, workout.getDifficulty());
            stmt.setInt(5, workout.getDuration());
            stmt.setString(6, workout.getCategory());
            stmt.setBoolean(7, workout.isAiGenerated());
            stmt.setInt(8, workout.getId());
            
            stmt.executeUpdate();
            
            // Update exercises for this workout
            if (workout.getExercises() != null) {
                // First delete existing exercises
                exerciseDAO.deleteExercisesForWorkout(workout.getId());
                
                // Then save new exercises
                for (Exercise exercise : workout.getExercises()) {
                    exerciseDAO.saveExercise(exercise, workout.getId());
                }
            }
        } catch (SQLException e) {
            logger.error("Error updating workout", e);
            throw new RuntimeException("Database error updating workout", e);
        }
    }
    
    public void deleteWorkout(int workoutId) {
        // First delete exercises for this workout
        exerciseDAO.deleteExercisesForWorkout(workoutId);
        
        // Then delete the workout
        String query = "DELETE FROM workouts WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, workoutId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting workout", e);
            throw new RuntimeException("Database error deleting workout", e);
        }
    }
    
    private Workout mapResultSetToWorkout(ResultSet rs) throws SQLException {
        Workout workout = new Workout();
        workout.setId(rs.getInt("id"));
        workout.setName(rs.getString("name"));
        workout.setDescription(rs.getString("description"));
        workout.setTargetMuscleGroup(rs.getString("target_muscle_group"));
        workout.setDifficulty(rs.getString("difficulty"));
        workout.setDuration(rs.getInt("duration"));
        workout.setCategory(rs.getString("category"));
        workout.setAiGenerated(rs.getBoolean("is_ai_generated"));
        
        // Set other fields if they exist in the result set
        try {
            workout.setCaloriesBurn(rs.getInt("calories_burn"));
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }
        
        try {
            workout.setFavorite(rs.getBoolean("is_favorite"));
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }
        
        // Store creator_user_id in the workout model
        try {
            int creatorUserId = rs.getInt("creator_user_id");
            workout.setCreatorUserId(creatorUserId);
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            workout.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            workout.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return workout;
    }
    
    private List<Exercise> getExercisesForWorkout(int workoutId, Connection conn) throws SQLException {
        return exerciseDAO.getExercisesForWorkout(workoutId, conn);
    }
}
