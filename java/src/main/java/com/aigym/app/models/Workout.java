package com.aigym.app.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a workout in the system.
 */
public class Workout {
    private int id;
    private String name;
    private String description;
    private int duration; // in minutes
    private String difficulty; // e.g., "beginner", "intermediate", "advanced"
    private String category; // e.g., "strength", "cardio", "flexibility"
    private String targetMuscleGroup; // e.g., "chest", "legs", "full body"
    private int caloriesBurn; // estimated calories burned
    private boolean isFavorite;
    private boolean isAiGenerated;
    private int creatorUserId; // ID of the user who created the workout
    private List<Exercise> exercises;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Default constructor.
     */
    public Workout() {
        this.exercises = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isAiGenerated = false;
    }
    
    /**
     * Constructor with essential workout information.
     */
    public Workout(String name, String description, int duration, String difficulty, String category) {
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.difficulty = difficulty;
        this.category = category;
        this.exercises = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isAiGenerated = false;
    }
    
    /**
     * Full constructor with all workout attributes.
     */
    public Workout(int id, String name, String description, int duration, String difficulty, 
                  String category, String targetMuscleGroup, int caloriesBurn, boolean isFavorite, 
                  boolean isAiGenerated, int creatorUserId, List<Exercise> exercises,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.difficulty = difficulty;
        this.category = category;
        this.targetMuscleGroup = targetMuscleGroup;
        this.caloriesBurn = caloriesBurn;
        this.isFavorite = isFavorite;
        this.isAiGenerated = isAiGenerated;
        this.creatorUserId = creatorUserId;
        this.exercises = exercises != null ? exercises : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getTargetMuscleGroup() {
        return targetMuscleGroup;
    }

    public void setTargetMuscleGroup(String targetMuscleGroup) {
        this.targetMuscleGroup = targetMuscleGroup;
    }

    public int getCaloriesBurn() {
        return caloriesBurn;
    }

    public void setCaloriesBurn(int caloriesBurn) {
        this.caloriesBurn = caloriesBurn;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
    
    public boolean isAiGenerated() {
        return isAiGenerated;
    }

    public void setAiGenerated(boolean aiGenerated) {
        isAiGenerated = aiGenerated;
    }
    
    public int getCreatorUserId() {
        return creatorUserId;
    }

    public void setCreatorUserId(int creatorUserId) {
        this.creatorUserId = creatorUserId;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }
    
    public void addExercise(Exercise exercise) {
        this.exercises.add(exercise);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
