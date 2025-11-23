-- Flyway V2: Seed reference data (categories + seed user)
-- Database: MySQL 8+

-- Categories (default set from challenge)
INSERT INTO categories (name, slug)
VALUES
    ('Feature', 'feature'),
    ('UI', 'ui'),
    ('UX', 'ux'),
    ('Enhancement', 'enhancement'),
    ('Bug', 'bug');

-- Seed user (placeholder password hash: "password")
-- BCrypt hash taken from Spring samples: $2a$10$7EqJtq98hPqEX7fNZaFWoO5Y2F.WqS4xET/1EPYx5Q9Yq5eS8w8x2
INSERT INTO users (username, email, password, display_name, bio, avatar_url, role, enabled)
VALUES ('johndoe', 'john.doe@example.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5Y2F.WqS4xET/1EPYx5Q9Yq5eS8w8x2', 'John Doe', NULL, NULL, 'USER', TRUE),
    ('seeduser', 'seed@example.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5Y2F.WqS4xET/1EPYx5Q9Yq5eS8w8x2', 'Seed User', NULL, NULL, 'USER', TRUE);

