/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aigym.app.dao;

import com.aigym.app.models.Exercise;
import com.aigym.app.utils.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExerciseDAO {
    private static final Logger logger = LoggerFactory.getLogger(ExerciseDAO.class);
    private final DatabaseConnection dbConnection;
    
    public ExerciseDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    public Exercise getExerciseById(int exerciseId) {
        String query = "SELECT * FROM exercises WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, exerciseId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToExercise(rs);
            }
            
            return null;
        } catch (SQLException e) {
            logger.error("Error getting exercise by ID", e);
            throw new RuntimeException("Database error retrieving exercise", e);
        }
    }
    
    public List<Exercise> getExercisesForWorkout(int workoutId) {
        String query = "SELECT e.* FROM exercises e " +
                       "JOIN workout_exercises we ON e.id = we.exercise_id " +
                       "WHERE we.workout_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, workoutId);
            ResultSet rs = stmt.executeQuery();
            
            List<Exercise> exercises = new ArrayList<>();
            while (rs.next()) {
                exercises.add(mapResultSetToExercise(rs));
            }
            
            return exercises;
        } catch (SQLException e) {
            logger.error("Error getting exercises for workout", e);
            throw new RuntimeException("Database error retrieving exercises", e);
        }
    }
    
    public List<Exercise> getExercisesForWorkout(int workoutId, Connection conn) throws SQLException {
        String query = "SELECT e.* FROM exercises e " +
                       "JOIN workout_exercises we ON e.id = we.exercise_id " +
                       "WHERE we.workout_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, workoutId);
            ResultSet rs = stmt.executeQuery();
            
            List<Exercise> exercises = new ArrayList<>();
            while (rs.next()) {
                exercises.add(mapResultSetToExercise(rs));
            }
            
            return exercises;
        }
    }
    
    public List<Exercise> getAllExercises() {
        String query = "SELECT * FROM exercises";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            List<Exercise> exercises = new ArrayList<>();
            while (rs.next()) {
                exercises.add(mapResultSetToExercise(rs));
            }
            
            return exercises;
        } catch (SQLException e) {
            logger.error("Error getting all exercises", e);
            throw new RuntimeException("Database error retrieving exercises", e);
        }
    }
    
    public void saveExercise(Exercise exercise) {
        String query = "INSERT INTO exercises (name, description, muscle_group, equipment, sets, reps, duration) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, exercise.getName());
            stmt.setString(2, exercise.getDescription());
            stmt.setString(3, exercise.getMuscleGroup());
            stmt.setString(4, exercise.getEquipment());
            stmt.setInt(5, exercise.getSets());
            stmt.setInt(6, exercise.getReps());
            stmt.setInt(7, exercise.getDuration());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating exercise failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    exercise.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating exercise failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving exercise", e);
            throw new RuntimeException("Database error saving exercise", e);
        }
    }
    
    public void saveExercise(Exercise exercise, int workoutId) {
        // First save the exercise
        saveExercise(exercise);
        
        // Then link it to the workout
        String query = "INSERT INTO workout_exercises (workout_id, exercise_id, sets, reps, duration) " +
                       "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, workoutId);
            stmt.setInt(2, exercise.getId());
            stmt.setInt(3, exercise.getSets());
            stmt.setInt(4, exercise.getReps());
            stmt.setInt(5, exercise.getDuration());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error linking exercise to workout", e);
            throw new RuntimeException("Database error linking exercise to workout", e);
        }
    }
    
    public void deleteExercisesForWorkout(int workoutId) {
        String query = "DELETE FROM workout_exercises WHERE workout_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, workoutId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting exercises for workout", e);
            throw new RuntimeException("Database error deleting exercises", e);
        }
    }
    
    public Exercise mapResultSetToExercise(ResultSet rs) throws SQLException {
        Exercise exercise = new Exercise();
        exercise.setId(rs.getInt("id"));
        exercise.setName(rs.getString("name"));
        exercise.setDescription(rs.getString("description"));
        exercise.setMuscleGroup(rs.getString("muscle_group"));
        exercise.setEquipment(rs.getString("equipment"));
        
        // Set other fields if they exist in the result set
        try {
            exercise.setSets(rs.getInt("sets"));
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }
        
        try {
            exercise.setReps(rs.getInt("reps"));
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }
        
        try {
            exercise.setDuration(rs.getInt("duration"));
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }
        
        try {
            exercise.setVideoUrl(rs.getString("video_url"));
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }
        
        try {
            exercise.setImageUrl(rs.getString("image_url"));
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }
        
        return exercise;
    }
}