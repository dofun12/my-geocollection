-- Migration 002: Add user_id and is_home to points_of_interest
-- Description: Add user association and home POI functionality

-- Add user_id column (nullable first, then we'll update and make it NOT NULL)
ALTER TABLE points_of_interest
ADD COLUMN IF NOT EXISTS user_id BIGINT;

-- Add is_home column
ALTER TABLE points_of_interest
ADD COLUMN IF NOT EXISTS is_home BOOLEAN NOT NULL DEFAULT FALSE;

-- Assign all existing POIs to the default admin user
UPDATE points_of_interest
SET user_id = (SELECT id FROM users WHERE username = 'admin')
WHERE user_id IS NULL;

-- Now make user_id NOT NULL
ALTER TABLE points_of_interest
ALTER COLUMN user_id SET NOT NULL;

-- Add foreign key constraint
ALTER TABLE points_of_interest
ADD CONSTRAINT fk_poi_user
FOREIGN KEY (user_id) REFERENCES users(id)
ON DELETE CASCADE;

-- Create index on user_id for faster queries
CREATE INDEX IF NOT EXISTS idx_poi_user_id ON points_of_interest(user_id);

-- Create index on is_home for faster home POI lookups
CREATE INDEX IF NOT EXISTS idx_poi_is_home ON points_of_interest(user_id, is_home);
