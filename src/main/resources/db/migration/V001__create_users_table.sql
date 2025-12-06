-- Migration 001: Create users table
-- Description: Create users table for authentication

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on username for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- Create index on email for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Insert default admin user
-- Password: Admin@123 (hashed with BCrypt)
INSERT INTO users (username, email, password, created_at, updated_at)
VALUES (
    'admin',
    'admin@geocollection.com',
    '$2a$10$rO9hZz8IzpGDqkMJ5OhkdOYGhxWHGQEWp0Qr/Wn6PGUiQR3xPBhPa',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
