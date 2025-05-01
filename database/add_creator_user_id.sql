-- SQL script to add creator_user_id column to the workouts table

-- First check if the column already exists
SELECT COUNT(*) 
FROM information_schema.columns 
WHERE table_name = 'workouts' 
AND column_name = 'creator_user_id';

-- Add the creator_user_id column if it doesn't exist
ALTER TABLE workouts 
ADD COLUMN creator_user_id INT;

-- Add a foreign key constraint to reference the users table
ALTER TABLE workouts 
ADD CONSTRAINT fk_workout_creator 
FOREIGN KEY (creator_user_id) 
REFERENCES users(id);

-- Set a default value for existing records (optional)
-- This will set the creator_user_id to 1 for all existing workouts
-- You may want to adjust this based on your data
UPDATE workouts 
SET creator_user_id = 1 
WHERE creator_user_id IS NULL;

-- Make the column NOT NULL after setting default values
ALTER TABLE workouts 
MODIFY COLUMN creator_user_id INT NOT NULL;
