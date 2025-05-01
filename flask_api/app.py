from flask import Flask, request, jsonify
from flask_cors import CORS
import json
import random
from datetime import datetime
from recommendation_engine import generate_workout_recommendation, get_motivational_quote

app = Flask(__name__)
CORS(app)  # Enable CORS for all routes

@app.route('/api/health', methods=['GET'])
def health_check():
    return jsonify({"status": "healthy", "timestamp": datetime.now().isoformat()})

@app.route('/api/workout/recommend', methods=['POST'])
def recommend_workout():
    try:
        data = request.json
        user_id = data.get('userId')
        fitness_goal = data.get('fitnessGoal')
        fitness_level = data.get('fitnessLevel')
        
        # Additional parameters
        target_muscle_groups = data.get('targetMuscleGroups', [])
        available_equipment = data.get('availableEquipment', [])
        workout_duration = data.get('workoutDuration', 60)  # Default 60 minutes
        
        # Generate workout recommendation
        workout = generate_workout_recommendation(
            user_id, 
            fitness_goal, 
            fitness_level,
            target_muscle_groups,
            available_equipment,
            workout_duration
        )
        
        return jsonify(workout)
    except Exception as e:
        print(f"Error generating workout recommendation: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route('/api/workouts/generate', methods=['POST'])
def generate_workout():
    try:
        data = request.json
        prompt = data.get('prompt', '')
        user_id = data.get('userId')
        fitness_level = data.get('fitnessLevel')
        fitness_goal = data.get('fitnessGoal')
        
        print(f"Received workout generation request: {data}")
        
        # Extract target muscle groups from prompt if not provided
        target_muscle_groups = data.get('targetMuscleGroups', [])
        if not target_muscle_groups and isinstance(prompt, str):
            # Simple keyword extraction from prompt
            muscle_keywords = {
                "chest": "chest", "pectoral": "chest",
                "back": "back", "lats": "back",
                "leg": "legs", "quad": "legs", "hamstring": "legs",
                "arm": "arms", "bicep": "arms", "tricep": "arms",
                "shoulder": "shoulders", "delt": "shoulders",
                "core": "core", "ab": "core", "abdominal": "core"
            }
            
            prompt_lower = prompt.lower()
            for keyword, muscle in muscle_keywords.items():
                if keyword in prompt_lower and muscle not in target_muscle_groups:
                    target_muscle_groups.append(muscle)
        
        # If still no target muscle groups, default to full body
        if not target_muscle_groups:
            target_muscle_groups = ["full_body"]
        
        # Extract workout duration from prompt if not provided
        workout_duration = data.get('workoutDuration', 30)
        if isinstance(prompt, str):
            # Try to find duration in prompt
            import re
            duration_match = re.search(r'(\d+)\s*(?:min|minute)', prompt.lower())
            if duration_match:
                workout_duration = int(duration_match.group(1))
        
        # Generate workout recommendation
        workout = generate_workout_recommendation(
            user_id, 
            fitness_goal, 
            fitness_level,
            target_muscle_groups,
            None,  # available_equipment
            workout_duration
        )
        
        # Add the prompt to the workout description
        if workout.get('description') and prompt:
            workout['description'] = f"Based on your request: '{prompt}'\n\n{workout['description']}"
        
        return jsonify(workout)
    except Exception as e:
        print(f"Error generating workout recommendation: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route('/api/quote', methods=['GET'])
def get_quote():
    time_of_day = request.args.get('timeOfDay', 'ANY')  # 'MORNING', 'EVENING', or 'ANY'
    quote = get_motivational_quote(time_of_day)
    return jsonify(quote)

if __name__ == '__main__':
    app.run(debug=True, port=5000)