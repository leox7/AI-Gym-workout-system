import json
import random
import os
from datetime import datetime

# Sample data - in a real application, this would come from a database or ML model
EXERCISES = [
    {"id": 1, "name": "Push-ups", "targetMuscle": "chest", "exerciseType": "strength", "equipment": "bodyweight", "difficultyLevel": "beginner"},
    {"id": 2, "name": "Squats", "targetMuscle": "legs", "exerciseType": "strength", "equipment": "bodyweight", "difficultyLevel": "beginner"},
    {"id": 3, "name": "Pull-ups", "targetMuscle": "back", "exerciseType": "strength", "equipment": "bodyweight", "difficultyLevel": "intermediate"},
    {"id": 4, "name": "Bench Press", "targetMuscle": "chest", "exerciseType": "strength", "equipment": "barbell", "difficultyLevel": "intermediate"},
    {"id": 5, "name": "Deadlift", "targetMuscle": "back", "exerciseType": "strength", "equipment": "barbell", "difficultyLevel": "advanced"},
    {"id": 6, "name": "Lunges", "targetMuscle": "legs", "exerciseType": "strength", "equipment": "bodyweight", "difficultyLevel": "beginner"},
    {"id": 7, "name": "Plank", "targetMuscle": "core", "exerciseType": "strength", "equipment": "bodyweight", "difficultyLevel": "beginner"},
    {"id": 8, "name": "Treadmill Running", "targetMuscle": "cardio", "exerciseType": "cardio", "equipment": "machine", "difficultyLevel": "intermediate"},
    {"id": 9, "name": "Bicep Curls", "targetMuscle": "arms", "exerciseType": "strength", "equipment": "dumbbell", "difficultyLevel": "beginner"},
    {"id": 10, "name": "Shoulder Press", "targetMuscle": "shoulders", "exerciseType": "strength", "equipment": "dumbbell", "difficultyLevel": "intermediate"}
]

QUOTES = [
    {"quote": "The only bad workout is the one that didn't happen.", "author": "Unknown", "timeOfDay": "MORNING"},
    {"quote": "No pain, no gain.", "author": "Jane Fonda", "timeOfDay": "ANY"},
    {"quote": "Your body can stand almost anything. It's your mind that you have to convince.", "author": "Andrew Murphy", "timeOfDay": "ANY"},
    {"quote": "Good things come to those who sweat.", "author": "Unknown", "timeOfDay": "MORNING"},
    {"quote": "The hard days are the best because that's when champions are made.", "author": "Gabrielle Reece", "timeOfDay": "ANY"},
    {"quote": "Reflect on your day's achievements and rest well for tomorrow's challenges.", "author": "Unknown", "timeOfDay": "EVENING"},
    {"quote": "Tomorrow is another chance to get stronger, to eat better, to live healthier, and to be the best version of you.", "author": "Unknown", "timeOfDay": "EVENING"}
]

def generate_workout_recommendation(user_id, fitness_goal, fitness_level, target_muscle_groups=None, available_equipment=None, workout_duration=60):
    """
    Generate a personalized workout recommendation based on user parameters.
    
    In a real application, this would use machine learning models to generate truly personalized workouts.
    For this example, we'll use rule-based logic to create a reasonable workout plan.
    """
    if target_muscle_groups is None:
        target_muscle_groups = []
    
    if available_equipment is None:
        available_equipment = ["bodyweight"]
    
    # Filter exercises based on difficulty level
    suitable_exercises = [e for e in EXERCISES if is_suitable_difficulty(e["difficultyLevel"], fitness_level)]
    
    # Filter by equipment if specified
    if available_equipment:
        suitable_exercises = [e for e in suitable_exercises if e["equipment"] in available_equipment]
    
    # Filter by target muscle groups if specified
    if target_muscle_groups:
        suitable_exercises = [e for e in suitable_exercises if e["targetMuscle"] in target_muscle_groups]
    
    # If no suitable exercises found after filtering, use the original list
    if not suitable_exercises:
        suitable_exercises = EXERCISES
    
    # Select exercises for the workout
    num_exercises = min(len(suitable_exercises), 5)  # Maximum 5 exercises per workout
    selected_exercises = random.sample(suitable_exercises, num_exercises)
    
    # Create workout structure
    workout = {
        "id": random.randint(1000, 9999),  # Generate a random ID
        "name": generate_workout_name(fitness_goal, target_muscle_groups),
        "description": generate_workout_description(fitness_goal, fitness_level),
        "targetMuscleGroups": ",".join(target_muscle_groups) if target_muscle_groups else "full_body",
        "difficultyLevel": fitness_level,
        "durationMinutes": workout_duration,
        "workoutType": determine_workout_type(fitness_goal),
        "isAiGenerated": True,
        "creatorUserId": 0,  # System-generated
        "exercises": []
    }
    
    # Add exercises with specific parameters
    for exercise in selected_exercises:
        exercise_config = {
            "id": exercise["id"],
            "name": exercise["name"],
            "targetMuscle": exercise["targetMuscle"],
            "exerciseType": exercise["exerciseType"],
            "equipment": exercise["equipment"],
            "difficultyLevel": exercise["difficultyLevel"],
            "sets": determine_sets(fitness_level, exercise["exerciseType"]),
            "repsPerSet": determine_reps(fitness_goal, exercise["exerciseType"]),
            "durationMinutes": determine_duration(exercise["exerciseType"]),
            "weightKg": 0,  # This would be personalized in a real application
            "restBetweenSetsSeconds": determine_rest_time(fitness_goal, fitness_level)
        }
        workout["exercises"].append(exercise_config)
    
    return workout

def is_suitable_difficulty(exercise_difficulty, user_level):
    """Determine if an exercise's difficulty is suitable for the user's level."""
    difficulty_ranks = {"beginner": 1, "intermediate": 2, "advanced": 3}
    
    # Handle null values for exercise_difficulty and user_level
    if exercise_difficulty is None:
        exercise_difficulty = "beginner"
    
    if user_level is None:
        user_level = "intermediate"
    
    exercise_rank = difficulty_ranks.get(exercise_difficulty.lower(), 1)
    user_rank = difficulty_ranks.get(user_level.lower(), 1)
    
    # Allow exercises at or below user's level, or one level above
    return exercise_rank <= user_rank + 1

def generate_workout_name(fitness_goal, target_muscle_groups):
    """Generate a name for the workout based on goal and target muscles."""
    goal_prefixes = {
        "weight_loss": ["Fat-Burning", "Calorie-Crusher", "Weight Loss"],
        "muscle_gain": ["Muscle Builder", "Mass Gainer", "Strength"],
        "endurance": ["Endurance", "Stamina", "Conditioning"],
        "general_fitness": ["Total Body", "Fitness Booster", "Well-Rounded"]
    }
    
    prefix = random.choice(goal_prefixes.get(fitness_goal, goal_prefixes["general_fitness"]))
    
    if target_muscle_groups and len(target_muscle_groups) == 1:
        return f"{prefix} {target_muscle_groups[0].title()} Workout"
    elif target_muscle_groups and len(target_muscle_groups) > 1:
        return f"{prefix} {' & '.join([g.title() for g in target_muscle_groups[:2]])} Workout"
    else:
        return f"{prefix} Full Body Workout"

def generate_workout_description(fitness_goal, fitness_level):
    """Generate a description for the workout."""
    descriptions = {
        "weight_loss": {
            "beginner": "A beginner-friendly workout designed to boost metabolism and burn calories. Focus on form and gradually increase intensity.",
            "intermediate": "A moderate-intensity workout to maximize calorie burn and fat loss. Keep rest periods short to maintain elevated heart rate.",
            "advanced": "A high-intensity workout for experienced athletes looking to shed fat while maintaining muscle mass."
        },
        "muscle_gain": {
            "beginner": "An introductory strength workout focused on building foundational muscle and proper form.",
            "intermediate": "A balanced hypertrophy workout designed to stimulate muscle growth with moderate weights and controlled movements.",
            "advanced": "An intensive muscle-building workout utilizing advanced techniques to maximize hypertrophy."
        },
        "endurance": {
            "beginner": "A starter endurance workout to build cardiovascular fitness and muscular stamina.",
            "intermediate": "A challenging endurance circuit to improve stamina and cardiovascular health.",
            "advanced": "A high-level endurance workout designed to push your limits and maximize stamina."
        },
        "general_fitness": {
            "beginner": "A well-rounded workout for beginners to improve overall fitness and establish healthy exercise habits.",
            "intermediate": "A balanced workout combining strength and cardio elements for comprehensive fitness improvement.",
            "advanced": "A challenging full-body workout designed to maintain peak physical condition across all fitness domains."
        }
    }
    
    return descriptions.get(fitness_goal, descriptions["general_fitness"]).get(fitness_level, descriptions["general_fitness"]["intermediate"])

def determine_workout_type(fitness_goal):
    """Determine the primary workout type based on fitness goal."""
    workout_types = {
        "weight_loss": "cardio",
        "muscle_gain": "strength",
        "endurance": "cardio",
        "general_fitness": "hybrid"
    }
    return workout_types.get(fitness_goal, "hybrid")

def determine_sets(fitness_level, exercise_type):
    """Determine the number of sets based on fitness level and exercise type."""
    if exercise_type == "cardio":
        return 1
    
    sets_by_level = {
        "beginner": 3,
        "intermediate": 4,
        "advanced": 5
    }
    return sets_by_level.get(fitness_level, 3)

def determine_reps(fitness_goal, exercise_type):
    """Determine the number of reps based on fitness goal and exercise type."""
    if exercise_type == "cardio":
        return 0
    
    reps_by_goal = {
        "weight_loss": 15,
        "muscle_gain": 8,
        "endurance": 20,
        "general_fitness": 12
    }
    return reps_by_goal.get(fitness_goal, 12)

def determine_duration(exercise_type):
    """Determine the duration for cardio exercises."""
    if exercise_type == "cardio":
        return random.randint(15, 30)
    return 0

def determine_rest_time(fitness_goal, fitness_level):
    """Determine rest time between sets based on fitness goal and level."""
    if fitness_goal == "weight_loss":
        return 30  # Shorter rest for weight loss
    elif fitness_goal == "muscle_gain":
        return 90  # Longer rest for muscle gain
    elif fitness_goal == "endurance":
        return 20  # Very short rest for endurance
    
    # Default rest times by fitness level
    rest_by_level = {
        "beginner": 60,
        "intermediate": 45,
        "advanced": 30
    }
    return rest_by_level.get(fitness_level, 45)

def get_motivational_quote(time_of_day="ANY"):
    """Get a motivational quote appropriate for the time of day."""
    suitable_quotes = [q for q in QUOTES if q["timeOfDay"] == time_of_day or q["timeOfDay"] == "ANY"]
    
    if not suitable_quotes:
        suitable_quotes = QUOTES
    
    return random.choice(suitable_quotes)