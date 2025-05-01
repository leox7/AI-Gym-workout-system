-- Drop database if it exists (be careful with this in production!)
DROP DATABASE IF EXISTS aigym_db;

-- Create database
CREATE DATABASE aigym_db;
USE aigym_db;

-- Create users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    age INT,
    height FLOAT,
    weight FLOAT,
    fitness_level ENUM('Beginner', 'Intermediate', 'Advanced'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create workouts table
CREATE TABLE workouts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    duration INT,
    difficulty ENUM('Beginner', 'Intermediate', 'Advanced'),
    category VARCHAR(50),
    calories_burn INT,
    is_public BOOLEAN DEFAULT FALSE,
    target_muscle_group VARCHAR(100),
    is_ai_generated BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create exercises table
CREATE TABLE exercises (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    muscle_group VARCHAR(50),
    equipment VARCHAR(100),
    sets INT DEFAULT 3,
    reps INT DEFAULT 10,
    duration INT DEFAULT 0,
    video_url VARCHAR(255),
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create workout_exercises junction table
CREATE TABLE workout_exercises (
    workout_id INT,
    exercise_id INT,
    exercise_order INT NOT NULL,
    PRIMARY KEY (workout_id, exercise_id),
    FOREIGN KEY (workout_id) REFERENCES workouts(id) ON DELETE CASCADE,
    FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE
);

-- Create user_favorite_workouts junction table
CREATE TABLE user_favorite_workouts (
    user_id INT,
    workout_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, workout_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (workout_id) REFERENCES workouts(id) ON DELETE CASCADE
);

-- Create workout_history table
CREATE TABLE workout_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    workout_id INT,
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    duration_minutes INT,
    calories_burned INT,
    notes TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (workout_id) REFERENCES workouts(id) ON DELETE SET NULL
);

-- Create user_settings table
CREATE TABLE user_settings (
    user_id INT PRIMARY KEY,
    theme VARCHAR(20) DEFAULT 'light',
    notification_enabled BOOLEAN DEFAULT TRUE,
    measurement_unit ENUM('Metric', 'Imperial') DEFAULT 'Metric',
    preferred_workout_duration INT DEFAULT 30,
    preferred_workout_difficulty ENUM('Beginner', 'Intermediate', 'Advanced') DEFAULT 'Intermediate',
    preferred_workout_days SET('Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday'),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create workout templates table
CREATE TABLE workout_templates (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    difficulty ENUM('Beginner', 'Intermediate', 'Advanced'),
    category VARCHAR(50),
    target_muscle_group VARCHAR(100),
    is_system_template BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Add indexes for performance
CREATE INDEX idx_workouts_user_id ON workouts(user_id);
CREATE INDEX idx_workouts_difficulty ON workouts(difficulty);
CREATE INDEX idx_workouts_category ON workouts(category);
CREATE INDEX idx_workouts_target_muscle_group ON workouts(target_muscle_group);
CREATE INDEX idx_exercises_muscle_group ON exercises(muscle_group);
CREATE INDEX idx_workout_history_user_id ON workout_history(user_id);
CREATE INDEX idx_workout_history_completed_at ON workout_history(completed_at);

-- Add full-text search capabilities
ALTER TABLE workouts ADD FULLTEXT(name, description);
ALTER TABLE exercises ADD FULLTEXT(name, description);

-- Insert a test user (password: password123)
INSERT INTO users (username, email, password_hash, full_name, age, height, weight, fitness_level)
VALUES ('testuser', 'test@example.com', '$2a$10$rPiEAgQNIT1TCoQk.CpYZ.lZ7b4.mKfBOWYkbXsJH6YLTzXYLbPZS', 'Test User', 30, 175, 70, 'Intermediate');

-- Insert default settings for test user
INSERT INTO user_settings (user_id, theme, notification_enabled, measurement_unit)
VALUES (1, 'light', TRUE, 'Metric');