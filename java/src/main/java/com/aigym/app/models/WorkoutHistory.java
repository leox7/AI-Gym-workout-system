/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aigym.app.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model class representing a completed workout session.
 */
public class WorkoutHistory {
    private int id;
    private int userId;
    private int workoutId;
    private String workoutName;
    private LocalDate date;
    private int duration; // in minutes
    private int caloriesBurned;
    private String status; // e.g., "completed", "partial", "skipped"
    private String notes;
;
    
    /**
     * Default constructor.
     */
    public WorkoutHistory() {
        this.date = LocalDate.now();
      
    }
    
    /**
     * Constructor with essential workout history information.
     */
    public WorkoutHistory(int userId, int workoutId, String workoutName, int duration, int caloriesBurned, String status) {
        this.userId = userId;
        this.workoutId = workoutId;
        this.workoutName = workoutName;
        this.date = LocalDate.now();
        this.duration = duration;
        this.caloriesBurned = caloriesBurned;
        this.status = status;

    }
    
    /**
     * Full constructor with all workout history attributes.
     */
    public WorkoutHistory(int id, int userId, int workoutId, String workoutName, LocalDate date,
                         int duration, int caloriesBurned, String status, String notes ) {
        this.id = id;
        this.userId = userId;
        this.workoutId = workoutId;
        this.workoutName = workoutName;
        this.date = date;
        this.duration = duration;
        this.caloriesBurned = caloriesBurned;
        this.status = status;
        this.notes = notes;
 
    }
    
    // Getters and Setters
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(int workoutId) {
        this.workoutId = workoutId;
    }

    public String getWorkoutName() {
        return workoutName;
    }

    public void setWorkoutName(String workoutName) {
        this.workoutName = workoutName;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    /**
     * Sets the duration in minutes (alias for setDuration)
     * @param durationMinutes The duration in minutes
     */
    public void setDurationMinutes(int durationMinutes) {
        this.duration = durationMinutes;
    }
    
    /**
     * Gets the duration in minutes (alias for getDuration)
     * @return The duration in minutes
     */
    public int getDurationMinutes() {
        return this.duration;
    }

    public int getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(int caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }




}
