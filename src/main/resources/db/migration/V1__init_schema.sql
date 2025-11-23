-- Flyway V1: Initial schema for Product Feedback App
-- Database: MySQL 8+
-- Notes:
--  - All FK relations use ON DELETE CASCADE where child rows should be removed with parent.
--  - Common query indexes included for status/category/author lookups and counts.

-- Ensure SQL mode for deterministic timestamp defaults
SET sql_mode = 'STRICT_ALL_TABLES';

-- USERS
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password      VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100) NULL,
    bio           VARCHAR(500) NULL,
    avatar_url    VARCHAR(500) NULL,
    role          VARCHAR(32)  NOT NULL DEFAULT 'USER',
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- CATEGORIES
CREATE TABLE IF NOT EXISTS categories (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(120) NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_categories_slug UNIQUE (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- FEEDBACK
-- status uses ENUM per guidelines
CREATE TABLE IF NOT EXISTS feedback (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    title          VARCHAR(200) NOT NULL,
    description    TEXT         NOT NULL,
    status         ENUM('SUGGESTION','PLANNED','IN_PROGRESS','LIVE') NOT NULL DEFAULT 'SUGGESTION',
    comment_count  INT          NOT NULL DEFAULT 0,
    upvote_count   INT          NOT NULL DEFAULT 0,
    author_id      BIGINT       NOT NULL,
    category_id    BIGINT       NOT NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedback_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_feedback_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_feedback_status ON feedback(status);
CREATE INDEX idx_feedback_category ON feedback(category_id);
CREATE INDEX idx_feedback_author ON feedback(author_id);

-- COMMENTS
CREATE TABLE IF NOT EXISTS comments (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    content     TEXT      NOT NULL,
    feedback_id BIGINT    NOT NULL,
    author_id   BIGINT    NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_feedback FOREIGN KEY (feedback_id) REFERENCES feedback(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_comments_feedback ON comments(feedback_id);
CREATE INDEX idx_comments_author ON comments(author_id);

-- REPLIES
CREATE TABLE IF NOT EXISTS replies (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    content          TEXT      NOT NULL,
    comment_id       BIGINT    NOT NULL,
    author_id        BIGINT    NOT NULL,
    reply_to_user_id BIGINT    NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_replies_comment FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    CONSTRAINT fk_replies_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_replies_reply_to_user FOREIGN KEY (reply_to_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_replies_comment ON replies(comment_id);
CREATE INDEX idx_replies_author ON replies(author_id);

-- UPVOTES (composite PK user_id + feedback_id)
CREATE TABLE IF NOT EXISTS upvotes (
    user_id     BIGINT    NOT NULL,
    feedback_id BIGINT    NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, feedback_id),
    CONSTRAINT fk_upvotes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_upvotes_feedback FOREIGN KEY (feedback_id) REFERENCES feedback(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_upvotes_feedback ON upvotes(feedback_id);
