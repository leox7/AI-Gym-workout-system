USE aigym_db;
ALTER TABLE users 
ADD COLUMN first_name VARCHAR(50) AFTER password_hash,
ADD COLUMN last_name VARCHAR(50) AFTER first_name,
ADD COLUMN date_of_birth DATE AFTER last_name,
ADD COLUMN gender VARCHAR(20) AFTER date_of_birth,
ADD COLUMN fitness_goal VARCHAR(50) AFTER fitness_level,
ADD COLUMN role VARCHAR(20) DEFAULT 'MEMBER' AFTER fitness_goal;


-- SQL script to add creator_user_id column to the workouts table
USE aigym_db;

SELECT COUNT(*) 
FROM information_schema.columns 
WHERE table_name = 'workouts' 
AND column_name = 'creator_user_id';

-- Add the creator_user_id column if it doesn't exist
ALTER TABLE workouts 
ADD COLUMN creator_user_id INT NULL;

-- Set a default value for existing records (optional)
-- This will set the creator_user_id to 1 for all existing workouts
-- You may want to adjust this based on your data
UPDATE workouts 
SET creator_user_id = 1 
WHERE creator_user_id IS NULL;

-- Drop the foreign key constraint if it exists
-- (This is needed before modifying the column)
SET @constraint_exists = (
    SELECT COUNT(*) 
    FROM information_schema.table_constraints 
    WHERE constraint_name = 'fk_workout_creator' 
    AND table_name = 'workouts'
);

SET @sql = IF(@constraint_exists > 0, 
              'ALTER TABLE workouts DROP FOREIGN KEY fk_workout_creator', 
              'SELECT 1');
              
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Make the column NOT NULL after setting default values
ALTER TABLE workouts 
MODIFY COLUMN creator_user_id INT NOT NULL;

-- Add the foreign key constraint back
ALTER TABLE workouts 
ADD CONSTRAINT fk_workout_creator 
FOREIGN KEY (creator_user_id) 
REFERENCES users(id);