/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aigym.app.services;

import com.aigym.app.models.Exercise;
import com.aigym.app.models.Workout;
import com.aigym.app.utils.DatabaseConnection;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WorkoutService {
    private static final Logger logger = LoggerFactory.getLogger(WorkoutService.class);
    private final DatabaseConnection dbConnection;
    
    public WorkoutService() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    public List<Workout> getUserWorkouts(int userId) {
        String query = "SELECT * FROM workouts WHERE user_id = ? OR is_public = true ORDER BY created_at DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            List<Workout> workouts = new ArrayList<>();
            while (rs.next()) {
                try {
                    Workout workout = mapResultSetToWorkout(rs);
                    // Don't fetch exercises yet, just add the workout
                    workouts.add(workout);
                } catch (SQLException e) {
                    // Log the error but continue processing other workouts
                    logger.error("Error mapping workout from result set: {}", e.getMessage());
                }
            }
            
            // Now fetch exercises for all workouts after the main ResultSet is closed
            for (Workout workout : workouts) {
                try {
                    workout.setExercises(getWorkoutExercises(workout.getId()));
                } catch (Exception e) {
                    logger.error("Error fetching exercises for workout {}: {}", workout.getId(), e.getMessage());
                    // Set empty list if there's an error
                    workout.setExercises(new ArrayList<>());
                }
            }
            
            return workouts;
        } catch (SQLException e) {
            logger.error("Error retrieving user workouts: {}", e.getMessage());
            // Return an empty list instead of throwing an exception
            return new ArrayList<>();
        }
    }
    
    public List<Workout> getUserFavoriteWorkouts(int userId) {
        String query = "SELECT w.* FROM workouts w " +
                       "JOIN user_favorite_workouts uf ON w.id = uf.workout_id " +
                       "WHERE uf.user_id = ? ORDER BY w.name";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            List<Workout> workouts = new ArrayList<>();
            while (rs.next()) {
                try {
                    Workout workout = mapResultSetToWorkout(rs);
                    workout.setFavorite(true);
                    // Don't fetch exercises yet, just add the workout
                    workouts.add(workout);
                } catch (SQLException e) {
                    logger.error("Error mapping favorite workout from result set: {}", e.getMessage());
                }
            }
            
            // Now fetch exercises for all workouts after the main ResultSet is closed
            for (Workout workout : workouts) {
                try {
                    workout.setExercises(getWorkoutExercises(workout.getId()));
                } catch (Exception e) {
                    logger.error("Error fetching exercises for workout {}: {}", workout.getId(), e.getMessage());
                    // Set empty list if there's an error
                    workout.setExercises(new ArrayList<>());
                }
            }
            
            return workouts;
        } catch (SQLException e) {
            logger.error("Error retrieving user favorite workouts: {}", e.getMessage());
            // Return an empty list instead of throwing an exception
            return new ArrayList<>();
        }
    }
    
    public List<Workout> getUserCompletedWorkouts(int userId) {
        String query = "SELECT DISTINCT w.* FROM workouts w " +
                       "JOIN workout_history wh ON w.id = wh.workout_id " +
                       "WHERE wh.user_id = ? AND wh.status = 'completed' " +
                       "ORDER BY w.name";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            List<Workout> workouts = new ArrayList<>();
            while (rs.next()) {
                Workout workout = mapResultSetToWorkout(rs);
                workout.setExercises(getWorkoutExercises(workout.getId()));
                workouts.add(workout);
            }
            
            return workouts;
        } catch (SQLException e) {
            logger.error("Error retrieving user completed workouts", e);
            throw new RuntimeException("Database error while retrieving completed workouts", e);
        }
    }
    
    public Workout getWorkoutById(int workoutId) {
        String query = "SELECT * FROM workouts WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, workoutId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Workout workout = mapResultSetToWorkout(rs);
                workout.setExercises(getWorkoutExercises(workoutId));
                return workout;
            }
            
            return null;
        } catch (SQLException e) {
            logger.error("Error retrieving workout by ID", e);
            throw new RuntimeException("Database error while retrieving workout", e);
        }
    }
    
    public Workout createWorkout(Workout workout, int userId) {
        // Check if the target_muscle_group and is_ai_generated columns exist
        boolean hasNewColumns = checkIfColumnsExist();
        
        String query;
        if (hasNewColumns) {
            query = "INSERT INTO workouts (name, description, duration, difficulty, category, " +
                   "target_muscle_group, calories_burn, user_id, is_ai_generated, creator_user_id, " +
                   "is_public, created_at, updated_at) " +
                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            query = "INSERT INTO workouts (name, description, duration, difficulty, category, " +
                   "calories_burn, user_id, is_public, created_at, updated_at) " +
                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, workout.getName());
            stmt.setString(2, workout.getDescription());
            stmt.setInt(3, workout.getDuration());
            stmt.setString(4, workout.getDifficulty());
            stmt.setString(5, workout.getCategory());
            
            int paramIndex = 6;
            if (hasNewColumns) {
                stmt.setString(paramIndex++, workout.getTargetMuscleGroup());
                stmt.setInt(paramIndex++, workout.getCaloriesBurn());
                stmt.setInt(paramIndex++, userId);
                stmt.setBoolean(paramIndex++, workout.isAiGenerated());
                
                // Set creator_user_id (if not set, use the current user's ID)
                int creatorId = workout.getCreatorUserId();
                if (creatorId <= 0) {
                    creatorId = userId;
                }
                stmt.setInt(paramIndex++, creatorId);
            } else {
                stmt.setInt(paramIndex++, workout.getCaloriesBurn());
                stmt.setInt(paramIndex++, userId);
            }
            
            stmt.setBoolean(paramIndex++, false); // Default to private workout
            stmt.setTimestamp(paramIndex++, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(paramIndex++, Timestamp.valueOf(LocalDateTime.now()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating workout failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    workout.setId(generatedKeys.getInt(1));
                    
                    // Save exercises
                    for (Exercise exercise : workout.getExercises()) {
                        addExerciseToWorkout(workout.getId(), exercise);
                    }
                    
                    return workout;
                } else {
                    throw new SQLException("Creating workout failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            logger.error("Error creating workout: {}", e.getMessage());
            throw new RuntimeException("Database error while creating workout", e);
        }
    }
    
    /**
     * Check if the target_muscle_group and is_ai_generated columns exist in the workouts table
     */
    private boolean checkIfColumnsExist() {
        String query = "SELECT COUNT(*) FROM information_schema.columns " +
                      "WHERE table_name = 'workouts' AND column_name IN ('target_muscle_group', 'is_ai_generated')";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count == 2; // Both columns exist
            }
            
            return false;
        } catch (SQLException e) {
            logger.error("Error checking if columns exist: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean toggleFavoriteWorkout(int userId, int workoutId, boolean isFavorite) {
        if (isFavorite) {
            // First check if the workout is already a favorite to avoid duplicate key errors
            String checkQuery = "SELECT COUNT(*) FROM user_favorite_workouts WHERE user_id = ? AND workout_id = ?";
            
            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                
                checkStmt.setInt(1, userId);
                checkStmt.setInt(2, workoutId);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next() && rs.getInt(1) > 0) {
                    // Already a favorite, no need to insert
                    logger.info("Workout {} is already a favorite for user {}", workoutId, userId);
                    return true;
                }
                
                // Not a favorite yet, insert it
                String insertQuery = "INSERT INTO user_favorite_workouts (user_id, workout_id) VALUES (?, ?)";
                
                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setInt(2, workoutId);
                    insertStmt.executeUpdate();
                    logger.info("Added workout {} to favorites for user {}", workoutId, userId);
                    return true;
                }
            } catch (SQLException e) {
                logger.error("Error adding workout to favorites: {}", e.getMessage());
                // Return false instead of throwing an exception
                return false;
            }
        } else {
            String query = "DELETE FROM user_favorite_workouts WHERE user_id = ? AND workout_id = ?";
            
            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setInt(1, userId);
                stmt.setInt(2, workoutId);
                stmt.executeUpdate();
                logger.info("Removed workout {} from favorites for user {}", workoutId, userId);
                return true;
            } catch (SQLException e) {
                logger.error("Error removing workout from favorites: {}", e.getMessage());
                // Return false instead of throwing an exception
                return false;
            }
        }
    }
    
    private List<Exercise> getWorkoutExercises(int workoutId) {
        String query = "SELECT e.* FROM exercises e " +
                       "JOIN workout_exercises we ON e.id = we.exercise_id " +
                       "WHERE we.workout_id = ? ORDER BY we.exercise_order";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, workoutId);
            ResultSet rs = stmt.executeQuery();
            
            List<Exercise> exercises = new ArrayList<>();
            while (rs.next()) {
                Exercise exercise = mapResultSetToExercise(rs);
                exercises.add(exercise);
            }
            
            return exercises;
        } catch (SQLException e) {
            logger.error("Error retrieving workout exercises", e);
            throw new RuntimeException("Database error while retrieving exercises", e);
        }
    }
    
    private void addExerciseToWorkout(int workoutId, Exercise exercise) {
        // Validate input parameters
        if (workoutId <= 0) {
            logger.error("Invalid workout ID: {}", workoutId);
            throw new IllegalArgumentException("Workout ID must be positive");
        }
        
        if (exercise == null) {
            logger.error("Exercise cannot be null");
            throw new IllegalArgumentException("Exercise cannot be null");
        }
        
        // Check if the workout exists
        try {
            if (getWorkoutById(workoutId) == null) {
                logger.error("Workout with ID {} does not exist", workoutId);
                throw new IllegalArgumentException("Workout does not exist");
            }
        } catch (Exception e) {
            logger.error("Error checking workout existence: {}", e.getMessage());
            throw new RuntimeException("Database error while checking workout existence", e);
        }
        
        // Ensure workout_exercises table exists
        ensureWorkoutExercisesTableExists();
        
        // First, save the exercise if it doesn't have an ID
        if (exercise.getId() == 0) {
            String query = "INSERT INTO exercises (name, description, muscle_group, equipment, " +
                           "sets, reps, duration, video_url, image_url) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                
                stmt.setString(1, exercise.getName());
                stmt.setString(2, exercise.getDescription());
                stmt.setString(3, exercise.getMuscleGroup());
                stmt.setString(4, exercise.getEquipment());
                stmt.setInt(5, exercise.getSets());
                stmt.setInt(6, exercise.getReps());
                stmt.setInt(7, exercise.getDuration());
                stmt.setString(8, exercise.getVideoUrl());
                stmt.setString(9, exercise.getImageUrl());
                
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
                logger.error("Error creating exercise: {}", e.getMessage());
                throw new RuntimeException("Database error while creating exercise", e);
            }
        }
        
        // Now, link the exercise to the workout
        String query = "INSERT INTO workout_exercises (workout_id, exercise_id, exercise_order) " +
                       "VALUES (?, ?, ?)";
        
        try {
            // Get the next exercise order first, outside the try-with-resources block
            int nextExerciseOrder = getNextExerciseOrder(workoutId);
            
            // Now perform the insert with a new connection
            try (Connection conn = dbConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setInt(1, workoutId);
                stmt.setInt(2, exercise.getId());
                stmt.setInt(3, nextExerciseOrder);
                
                stmt.executeUpdate();
                logger.info("Successfully added exercise {} to workout {} with order {}", 
                        exercise.getId(), workoutId, nextExerciseOrder);
            }
        } catch (SQLException e) {
            logger.error("Error linking exercise to workout: {} - SQL State: {}, Error Code: {}", 
                    e.getMessage(), e.getSQLState(), e.getErrorCode());
            throw new RuntimeException("Database error while linking exercise to workout", e);
        }
    }
    
    /**
     * Ensures that the workout_exercises table exists in the database.
     * Creates the table if it doesn't exist.
     */
    private void ensureWorkoutExercisesTableExists() {
        String createTableSQL = 
            "CREATE TABLE IF NOT EXISTS workout_exercises (" +
            "workout_id INT, " +
            "exercise_id INT, " +
            "exercise_order INT NOT NULL, " +
            "PRIMARY KEY (workout_id, exercise_id), " +
            "FOREIGN KEY (workout_id) REFERENCES workouts(id) ON DELETE CASCADE, " +
            "FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE" +
            ")";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(createTableSQL);
            logger.info("Ensured workout_exercises table exists");
        } catch (SQLException e) {
            logger.error("Error ensuring workout_exercises table exists: {}", e.getMessage());
            throw new RuntimeException("Database error while ensuring workout_exercises table exists", e);
        }
    }
    
    private int getNextExerciseOrder(int workoutId) {
        String query = "SELECT MAX(exercise_order) FROM workout_exercises WHERE workout_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, workoutId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int maxOrder = rs.getInt(1);
                    return maxOrder + 1;
                } else {
                    return 1; // First exercise in the workout
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting next exercise order", e);
            return 1; // Default to 1 in case of error
        }
    }
    
    /**
     * Adds an exercise to an existing workout after verifying the workout exists
     * 
     * @param workoutId The ID of the workout to add the exercise to
     * @param exercise The exercise to add
     * @return true if the exercise was successfully added, false otherwise
     */
    public boolean addExerciseToExistingWorkout(int workoutId, Exercise exercise) {
        try {
            // First check if the workout exists
            Workout workout = getWorkoutById(workoutId);
            if (workout == null) {
                logger.error("Cannot add exercise to non-existent workout with ID: {}", workoutId);
                return false;
            }
            
            // Add the exercise to the workout
            addExerciseToWorkout(workoutId, exercise);
            return true;
        } catch (Exception e) {
            logger.error("Error adding exercise to workout {}: {}", workoutId, e.getMessage());
            return false;
        }
    }
    
    private Workout mapResultSetToWorkout(ResultSet rs) throws SQLException {
        Workout workout = new Workout();
        workout.setId(rs.getInt("id"));
        workout.setName(rs.getString("name"));
        workout.setDescription(rs.getString("description"));
        workout.setDuration(rs.getInt("duration"));
        workout.setDifficulty(rs.getString("difficulty"));
        workout.setCategory(rs.getString("category"));
        workout.setCaloriesBurn(rs.getInt("calories_burn"));
        
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
    
    private Exercise mapResultSetToExercise(ResultSet rs) throws SQLException {
        Exercise exercise = new Exercise();
        exercise.setId(rs.getInt("id"));
        exercise.setName(rs.getString("name"));
        exercise.setDescription(rs.getString("description"));
        exercise.setMuscleGroup(rs.getString("muscle_group"));
        exercise.setEquipment(rs.getString("equipment"));
        exercise.setSets(rs.getInt("sets"));
        exercise.setReps(rs.getInt("reps"));
        exercise.setDuration(rs.getInt("duration"));
        exercise.setVideoUrl(rs.getString("video_url"));
        exercise.setImageUrl(rs.getString("image_url"));
        
        return exercise;
    }
    
    public Workout convertJsonToWorkout(JSONObject json) {
        try {
            logger.info("Converting JSON to Workout: {}", json.toString());
            
            Workout workout = new Workout();
            
            // Map fields from the Flask API response to our Workout model
            if (json.has("name")) {
                workout.setName(json.getString("name"));
            } else {
                workout.setName("AI Generated Workout");
            }
            
            if (json.has("description")) {
                workout.setDescription(json.getString("description"));
            } else {
                workout.setDescription("Workout generated by AI based on your preferences");
            }
            
            if (json.has("duration")) {
                workout.setDuration(json.getInt("duration"));
            } else {
                workout.setDuration(30); // Default to 30 minutes
            }
            
            if (json.has("difficulty")) {
                workout.setDifficulty(json.getString("difficulty"));
            } else {
                workout.setDifficulty("intermediate");
            }
            
            if (json.has("category")) {
                workout.setCategory(json.getString("category"));
            } else {
                workout.setCategory("strength");
            }
            
            if (json.has("target_muscle_group")) {
                workout.setTargetMuscleGroup(json.getString("target_muscle_group"));
            } else if (json.has("targetMuscleGroup")) {
                workout.setTargetMuscleGroup(json.getString("targetMuscleGroup"));
            } else {
                workout.setTargetMuscleGroup("full_body");
            }
            
            if (json.has("calories_burn")) {
                workout.setCaloriesBurn(json.getInt("calories_burn"));
            } else if (json.has("caloriesBurn")) {
                workout.setCaloriesBurn(json.getInt("caloriesBurn"));
            } else {
                workout.setCaloriesBurn(300); // Default value
            }
            
            // Set AI generated flag
            workout.setAiGenerated(true);
            
            // Add exercises if they exist in the JSON
            if (json.has("exercises") && json.get("exercises") instanceof JSONArray) {
                JSONArray exercisesArray = json.getJSONArray("exercises");
                for (int i = 0; i < exercisesArray.length(); i++) {
                    JSONObject exerciseJson = exercisesArray.getJSONObject(i);
                    Exercise exercise = convertJsonToExercise(exerciseJson);
                    workout.addExercise(exercise);
                }
            }
            
            return workout;
        } catch (Exception e) {
            logger.error("Error converting JSON to Workout: {}", e.getMessage());
            throw new RuntimeException("Error converting JSON to Workout", e);
        }
    }
    
    private Exercise convertJsonToExercise(JSONObject json) {
        Exercise exercise = new Exercise();
        
        if (json.has("name")) {
            exercise.setName(json.getString("name"));
        }
        
        if (json.has("description")) {
            exercise.setDescription(json.getString("description"));
        }
        
        if (json.has("muscle_group")) {
            exercise.setMuscleGroup(json.getString("muscle_group"));
        } else if (json.has("muscleGroup")) {
            exercise.setMuscleGroup(json.getString("muscleGroup"));
        }
        
        if (json.has("equipment")) {
            exercise.setEquipment(json.getString("equipment"));
        }
        
        if (json.has("sets")) {
            exercise.setSets(json.getInt("sets"));
        }
        
        if (json.has("reps")) {
            exercise.setReps(json.getInt("reps"));
        }
        
        if (json.has("duration")) {
            exercise.setDuration(json.getInt("duration"));
        }
        
        if (json.has("video_url")) {
            exercise.setVideoUrl(json.getString("video_url"));
        } else if (json.has("videoUrl")) {
            exercise.setVideoUrl(json.getString("videoUrl"));
        }
        
        if (json.has("image_url")) {
            exercise.setImageUrl(json.getString("image_url"));
        } else if (json.has("imageUrl")) {
            exercise.setImageUrl(json.getString("imageUrl"));
        }
        
        return exercise;
    }
    
    /**
     * Calculate estimated calories burned based on workout duration and difficulty.
     * 
     * @param duration Workout duration in minutes
     * @param difficulty Workout difficulty level
     * @return Estimated calories burned
     */
    private int calculateEstimatedCalories(int duration, String difficulty) {
        int baseRate;
        
        // Determine base calorie burn rate per minute based on difficulty
        if (difficulty == null) {
            baseRate = 5; // Default
        } else if (difficulty.equalsIgnoreCase("beginner")) {
            baseRate = 4;
        } else if (difficulty.equalsIgnoreCase("intermediate")) {
            baseRate = 6;
        } else if (difficulty.equalsIgnoreCase("advanced")) {
            baseRate = 8;
        } else {
            baseRate = 5; // Default for unknown difficulty
        }
        
        return duration * baseRate;
    }
}
