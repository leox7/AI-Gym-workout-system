package com.aigym.app.models;

/**
 * Model class representing an exercise within a workout.
 */
public class Exercise {
    private int id;
    private String name;
    private String description;
    private String muscleGroup; // e.g., "chest", "legs", "back"
    private String equipment; // e.g., "dumbbell", "barbell", "bodyweight"
    private int sets;
    private int reps;
    private int duration; // in seconds, for timed exercises
    private String videoUrl; // URL to demonstration video
    private String imageUrl; // URL to demonstration image
    
    /**
     * Default constructor.
     */
    public Exercise() {
    }
    
    /**
     * Constructor with essential exercise information.
     */
    public Exercise(String name, String description, String muscleGroup, int sets, int reps) {
        this.name = name;
        this.description = description;
        this.muscleGroup = muscleGroup;
        this.sets = sets;
        this.reps = reps;
    }
    
    /**
     * Full constructor with all exercise attributes.
     */
    public Exercise(int id, String name, String description, String muscleGroup, String equipment,
                   int sets, int reps, int duration, String videoUrl, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.muscleGroup = muscleGroup;
        this.equipment = equipment;
        this.sets = sets;
        this.reps = reps;
        this.duration = duration;
        this.videoUrl = videoUrl;
        this.imageUrl = imageUrl;
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

    public String getMuscleGroup() {
        return muscleGroup;
    }

    public void setMuscleGroup(String muscleGroup) {
        this.muscleGroup = muscleGroup;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
