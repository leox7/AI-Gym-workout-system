## AI Gym Workout System💪
AI Gym Workout System is a comprehensive JavaFX application that empowers users to manage their fitness routines with the help of AI-powered workout recommendations. The system features a modern UI, robust workout tracking, and seamless integration with a Flask AI backend for personalized fitness suggestions.

## Features
User Authentication: Secure login and profile management.<br/>
Workout Management: Add, edit, and view workouts, including AI-generated routines.<br/>
Exercise Tracking: Log exercises, sets, reps, and weights.<br/>
Progress Monitoring: Visualize workout history and track fitness progress.<br/>
AI Recommendations: Get personalized workout plans based on your goals, powered by a Flask API.<br/>
Dashboard Navigation: Sidebar with intuitive navigation (Dashboard, Workouts, Progress, Profile, Logout).<br/>
MySQL Database: Persistent storage of users, workouts, and progress data.

## Tech Stack
Frontend: Java 17, JavaFX 17.0.2<br/>
Backend: Flask (Python 3.x, for AI recommendations)<br/>
Database: MySQL<br/>
Build System: Maven<br/>
Other: SLF4J for logging, FXML for UI layouts<br/>
Git

## Setup Instructions

1. Clone the Repository<br/>
git clone https://github.com/yourusername/AIGymWorkoutSystem.git<br/>
cd AIGymWorkoutSystem

2. Database Setup<br/>
Import the schema:<br/>
mysql -u root -p < database/aigym_db.sql <br/>
Update your MySQL credentials in the application config if necessary.

4. Flask API Backend <br/>
Navigate to the Flask backend directory (if provided) or set up your own Flask server for AI recommendations.<br/>
Install dependencies:<br/>
pip install flask <br/>
Run the Flask server:<br/>
python app.py 

5. Build and Run the JavaFX Application <br/>
Use Maven to build: <br/>
mvn clean install <br/>
Run the application using the provided script: <br/>
run.bat <br/>

Ensure your JavaFX and other dependencies are correctly set up in your environment. <br/>

 ## Configuration
JavaFX Modules: Ensure JavaFX libraries are on your module path. <br/>
Main Class: com.aigym.app.Launcher <br/>
Database: Update DB connection settings as needed. <br/>
Flask API: Configure the API endpoint in your JavaFX app to point to the running Flask server.

## Screenshots
![workout](https://github.com/user-attachments/assets/e1b5b1b3-703a-4381-a0e9-6c9e2ac4a50a)
![workout execution](https://github.com/user-attachments/assets/a77a1604-3cc2-42a2-86b8-02c127749715)
![exercise card](https://github.com/user-attachments/assets/0f97d401-d176-41e1-985d-743da2058598)
![dashboard](https://github.com/user-attachments/assets/1ca9af0c-f9d2-4ed6-a6ee-61f778db59cf)
![progress](https://github.com/user-attachments/assets/0eafb5c1-b6ff-41cc-a242-957d0836dae4)
![profile](https://github.com/user-attachments/assets/90136bef-c347-42e6-a080-3673da942694)
![Login UI](https://github.com/user-attachments/assets/a6386fb3-a625-4981-bb1d-7d313fb3193b)


## Contributing
Contributions are welcome! Please open issues or submit pull requests for improvements and bug fixes.

## Acknowledgments
JavaFX documentation <br/>
Flask documentation <br/>
Open-source fitness and UI design inspirations
