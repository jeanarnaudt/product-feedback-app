# Flyway Migration Plan

## 1. Naming & Locations

All migrations live in:

```text
src/main/resources/db/migration/
```

Files/classes:

* `V1__init_schema.sql`
* `V2__seed_reference_data.sql`
* `V3__seed_initial_feedback_from_json.java` (Java-based migration)

You’ll also need your `data.json` in:

```text
src/main/resources/data/data.json
```

---

## 2. `V1__init_schema.sql`

```sql
-- V1__init_schema.sql

-- Use utf8mb4 for full Unicode support
CREATE TABLE users (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    email        VARCHAR(255) NOT NULL UNIQUE,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role         VARCHAR(20)  NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    avatar_url   VARCHAR(255),
    bio          TEXT,
    created_at   DATETIME     NOT NULL,
    updated_at   DATETIME     NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE categories (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    slug     VARCHAR(100) NOT NULL UNIQUE,
    active   TINYINT(1)   NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE feedback (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(150) NOT NULL,
    description TEXT         NOT NULL,
    status      VARCHAR(30)  NOT NULL,
    category_id BIGINT       NOT NULL,
    author_id   BIGINT       NOT NULL,
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME     NOT NULL,
    CONSTRAINT fk_feedback_category
        FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_feedback_author
        FOREIGN KEY (author_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_feedback_status ON feedback (status);
CREATE INDEX idx_feedback_category ON feedback (category_id);
CREATE INDEX idx_feedback_author ON feedback (author_id);

CREATE TABLE comments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    content     TEXT    NOT NULL,
    feedback_id BIGINT  NOT NULL,
    author_id   BIGINT  NOT NULL,
    created_at  DATETIME NOT NULL,
    updated_at  DATETIME NOT NULL,
    CONSTRAINT fk_comments_feedback
        FOREIGN KEY (feedback_id) REFERENCES feedback (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_comments_author
        FOREIGN KEY (author_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_comments_feedback ON comments (feedback_id);
CREATE INDEX idx_comments_author ON comments (author_id);

CREATE TABLE replies (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    content          TEXT    NOT NULL,
    comment_id       BIGINT  NOT NULL,
    author_id        BIGINT  NOT NULL,
    reply_to_user_id BIGINT,
    created_at       DATETIME NOT NULL,
    updated_at       DATETIME NOT NULL,
    CONSTRAINT fk_replies_comment
        FOREIGN KEY (comment_id) REFERENCES comments (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_replies_author
        FOREIGN KEY (author_id) REFERENCES users (id),
    CONSTRAINT fk_replies_reply_to_user
        FOREIGN KEY (reply_to_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_replies_comment ON replies (comment_id);
CREATE INDEX idx_replies_author ON replies (author_id);

CREATE TABLE upvotes (
    user_id    BIGINT  NOT NULL,
    feedback_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (user_id, feedback_id),
    CONSTRAINT fk_upvotes_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_upvotes_feedback
        FOREIGN KEY (feedback_id) REFERENCES feedback (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_upvotes_feedback ON upvotes (feedback_id);
```

> Note: this schema matches the entities we discussed: `User`, `Category`, `Feedback`, `Comment`, `Reply`, `Upvote`.

---

## 3. `V2__seed_reference_data.sql`

This seeds **categories** and optionally a simple default user to attach seed feedback to later.

```sql
-- V2__seed_reference_data.sql

INSERT INTO categories (name, slug, active) VALUES
    ('Feature', 'feature', 1),
    ('UI', 'ui', 1),
    ('UX', 'ux', 1),
    ('Enhancement', 'enhancement', 1),
    ('Bug', 'bug', 1);

-- Optional: seed a generic user that can own the imported feedback.
-- For now, we insert with a placeholder password hash to be updated manually.
-- You can replace password_hash with a real BCrypt hash later.

INSERT INTO users (email, username, password_hash, role, display_name, avatar_url, bio, created_at, updated_at)
VALUES (
    'seeduser@example.com',
    'seeduser',
    '$2a$10$replace_this_with_real_bcrypt_hash__________',
    'USER',
    'Seed User',
    NULL,
    'Default user for imported seed feedback.',
    NOW(),
    NOW()
);
```

> You can delete the seed user later or update it to be an ADMIN.
> Also, consider generating a real BCrypt hash once you’ve wired Spring Security.

---

## 4. `V3__SeedInitialFeedbackFromJson.java` (Java-based migration)

This migration reads `data.json` and inserts `feedback`, `comments`, and `replies` using JDBC.
Package convention for Flyway Java migrations is `db.migration`.

```java
// src/main/java/db/migration/V3__SeedInitialFeedbackFromJson.java

package db.migration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Java-based migration to seed initial feedback, comments, and replies
 * from the Frontend Mentor data.json file.
 *
 * Assumptions:
 * - data.json is located at src/main/resources/data/data.json.
 * - Categories and a seed user already exist (from V2).
 */
public class V3__SeedInitialFeedbackFromJson extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        ObjectMapper objectMapper = new ObjectMapper();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("data/data.json")) {
            if (is == null) {
                throw new IllegalStateException("data/data.json not found on classpath");
            }

            Map<String, Object> root = objectMapper.readValue(is, new TypeReference<>() {});
            // Adjust the key here based on the actual JSON structure.
            // For the Frontend Mentor challenge, it's usually something like "productRequests".
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> productRequests =
                    (List<Map<String, Object>>) root.get("productRequests");

            long seedUserId = findSeedUserId(connection);

            for (Map<String, Object> request : productRequests) {
                insertFeedbackWithCommentsAndReplies(connection, request, seedUserId);
            }
        }
    }

    private long findSeedUserId(Connection connection) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT id FROM users WHERE username = ?")) {
            ps.setString(1, "seeduser");
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Seed user 'seeduser' not found. Run V2 first or adjust username.");
                }
                return rs.getLong("id");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void insertFeedbackWithCommentsAndReplies(
            Connection connection,
            Map<String, Object> request,
            long seedUserId
    ) throws Exception {

        String title = (String) request.get("title");
        String description = (String) request.get("description");
        String status = ((String) request.get("status")).toUpperCase().replace("-", "_"); // e.g. "in-progress" -> "IN_PROGRESS"
        Map<String, Object> categoryMap = (Map<String, Object>) request.get("category");
        String categoryName = categoryMap != null ? (String) categoryMap.get("name") : "Feature";

        long categoryId = findOrCreateCategory(connection, categoryName);

        LocalDateTime now = LocalDateTime.now();
        long feedbackId;

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO feedback (title, description, status, category_id, author_id, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                PreparedStatement.RETURN_GENERATED_KEYS
        )) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, status);
            ps.setLong(4, categoryId);
            ps.setLong(5, seedUserId);
            ps.setObject(6, now);
            ps.setObject(7, now);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Failed to insert feedback for title: " + title);
                }
                feedbackId = rs.getLong(1);
            }
        }

        // Handle comments & replies if present
        List<Map<String, Object>> comments =
                (List<Map<String, Object>>) request.get("comments");
        if (comments != null) {
            for (Map<String, Object> comment : comments) {
                insertCommentAndReplies(connection, comment, feedbackId, seedUserId);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void insertCommentAndReplies(
            Connection connection,
            Map<String, Object> comment,
            long feedbackId,
            long seedUserId
    ) throws Exception {

        String content = (String) comment.get("content");
        LocalDateTime now = LocalDateTime.now();
        long commentId;

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO comments (content, feedback_id, author_id, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?)",
                PreparedStatement.RETURN_GENERATED_KEYS
        )) {
            ps.setString(1, content);
            ps.setLong(2, feedbackId);
            ps.setLong(3, seedUserId);
            ps.setObject(4, now);
            ps.setObject(5, now);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Failed to insert comment for feedback_id: " + feedbackId);
                }
                commentId = rs.getLong(1);
            }
        }

        List<Map<String, Object>> replies =
                (List<Map<String, Object>>) comment.get("replies");
        if (replies != null) {
            for (Map<String, Object> reply : replies) {
                insertReply(connection, reply, commentId, seedUserId);
            }
        }
    }

    private void insertReply(
            Connection connection,
            Map<String, Object> reply,
            long commentId,
            long seedUserId
    ) throws Exception {

        String content = (String) reply.get("content");
        LocalDateTime now = LocalDateTime.now();
        // For simplicity, reply_to_user_id uses seedUserId.
        // You can later upgrade this to resolve real usernames from JSON.
        long replyToUserId = seedUserId;

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO replies (content, comment_id, author_id, reply_to_user_id, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?)"
        )) {
            ps.setString(1, content);
            ps.setLong(2, commentId);
            ps.setLong(3, seedUserId);
            ps.setLong(4, replyToUserId);
            ps.setObject(5, now);
            ps.setObject(6, now);
            ps.executeUpdate();
        }
    }

    private long findOrCreateCategory(Connection connection, String categoryName) throws Exception {
        String slug = categoryName.trim().toLowerCase().replace(" ", "-");

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT id FROM categories WHERE slug = ?")) {
            ps.setString(1, slug);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO categories (name, slug, active) VALUES (?, ?, 1)",
                PreparedStatement.RETURN_GENERATED_KEYS
        )) {
            ps.setString(1, categoryName);
            ps.setString(2, slug);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Failed to insert category: " + categoryName);
                }
                return rs.getLong(1);
            }
        }
    }
}
```

> You can refine this migration later to:
>
> * Map real users from `data.json` instead of using `seedUser` for everything.
> * Import upvote counts if you want (or just start with 0 and let real users vote).

---