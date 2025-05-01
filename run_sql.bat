@echo off
echo Running SQL script to add creator_user_id column to the workouts table...
mysql -u root -p"Askleon@07" aigym_db < add_creator_user_id.sql
if %ERRORLEVEL% EQU 0 (
    echo SQL script executed successfully!
) else (
    echo Error executing SQL script. Error code: %ERRORLEVEL%
)
pause
