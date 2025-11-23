package db.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;

/**
 * Flyway V3 Java-based migration to seed initial feedback/comments/replies from data.json.
 *
 * Notes:
 * - Uses a single seed user (prefers `seeduser`, falls back to `johndoe`) as the author for all content.
 * - Maps JSON status values to DB ENUM values.
 * - Calculates comment_count as comments + replies total, matching UI expectations.
 */
public class V3__SeedInitialFeedbackFromJson extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection conn = context.getConnection();

        long authorId = findSeedUserId(conn);

        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getClass().getResourceAsStream("/data/data.json")) {
            if (is == null) {
                throw new IllegalStateException("data.json not found at classpath:/data/data.json");
            }
            JsonNode root = mapper.readTree(is);
            JsonNode requests = root.path("productRequests");
            if (!requests.isArray()) {
                throw new IllegalStateException("data.json malformed: 'productRequests' array missing");
            }

            // Prepare statements we'll reuse
            try (PreparedStatement psFindCategory = conn.prepareStatement("SELECT id FROM categories WHERE slug = ?");
                 PreparedStatement psInsertFeedback = conn.prepareStatement(
                         "INSERT INTO feedback (title, description, status, comment_count, upvote_count, author_id, category_id) VALUES (?,?,?,?,?,?,?)",
                         Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psInsertComment = conn.prepareStatement(
                         "INSERT INTO comments (content, feedback_id, author_id) VALUES (?,?,?)",
                         Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psInsertReply = conn.prepareStatement(
                         "INSERT INTO replies (content, comment_id, author_id, reply_to_user_id) VALUES (?,?,?,NULL)");) {

                for (JsonNode item : requests) {
                    String title = item.path("title").asText();
                    String description = item.path("description").asText("");
                    String categorySlug = item.path("category").asText();
                    String status = mapStatus(item.path("status").asText());
                    int upvotes = item.path("upvotes").asInt(0);

                    long categoryId = findCategoryId(psFindCategory, categorySlug);

                    // compute comment_count = comments + replies
                    int commentCount = 0;
                    JsonNode comments = item.path("comments");
                    if (comments.isArray()) {
                        for (JsonNode c : comments) {
                            commentCount++; // the comment itself
                            JsonNode replies = c.path("replies");
                            if (replies.isArray()) {
                                commentCount += replies.size();
                            }
                        }
                    }

                    // Insert feedback
                    psInsertFeedback.setString(1, title);
                    psInsertFeedback.setString(2, description);
                    psInsertFeedback.setString(3, status);
                    psInsertFeedback.setInt(4, commentCount);
                    psInsertFeedback.setInt(5, upvotes);
                    psInsertFeedback.setLong(6, authorId);
                    psInsertFeedback.setLong(7, categoryId);
                    psInsertFeedback.executeUpdate();

                    long feedbackId;
                    try (ResultSet keys = psInsertFeedback.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new IllegalStateException("Failed to get generated key for feedback: " + title);
                        }
                        feedbackId = keys.getLong(1);
                    }

                    // Insert comments and replies (all authored by seed user)
                    if (comments.isArray()) {
                        for (JsonNode c : comments) {
                            String content = c.path("content").asText("");
                            if (content.isEmpty()) continue;
                            psInsertComment.setString(1, content);
                            psInsertComment.setLong(2, feedbackId);
                            psInsertComment.setLong(3, authorId);
                            psInsertComment.executeUpdate();

                            long commentId;
                            try (ResultSet k2 = psInsertComment.getGeneratedKeys()) {
                                if (!k2.next()) {
                                    throw new IllegalStateException("Failed to get generated key for comment");
                                }
                                commentId = k2.getLong(1);
                            }

                            JsonNode replies = c.path("replies");
                            if (replies.isArray()) {
                                for (Iterator<JsonNode> it = replies.elements(); it.hasNext(); ) {
                                    JsonNode r = it.next();
                                    String rContent = r.path("content").asText("");
                                    if (rContent.isEmpty()) continue;
                                    psInsertReply.setString(1, rContent);
                                    psInsertReply.setLong(2, commentId);
                                    psInsertReply.setLong(3, authorId);
                                    psInsertReply.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static long findSeedUserId(Connection conn) throws Exception {
        String sql = "SELECT id, username FROM users WHERE username IN ('seeduser','johndoe') ORDER BY CASE username WHEN 'seeduser' THEN 0 ELSE 1 END LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                throw new IllegalStateException("Seed user not found. Please ensure V2 inserted 'seeduser' or 'johndoe'.");
            }
            return rs.getLong("id");
        }
    }

    private static long findCategoryId(PreparedStatement psFindCategory, String slug) throws Exception {
        psFindCategory.setString(1, slug);
        try (ResultSet rs = psFindCategory.executeQuery()) {
            if (!rs.next()) {
                throw new IllegalStateException("Category not found for slug: " + slug);
            }
            return rs.getLong(1);
        }
    }

    private static String mapStatus(String jsonStatus) {
        if (jsonStatus == null) return "SUGGESTION";
        String s = jsonStatus.trim().toLowerCase();
        return switch (s) {
            case "suggestion" -> "SUGGESTION";
            case "planned" -> "PLANNED";
            case "in-progress", "in_progress", "in progress" -> "IN_PROGRESS";
            case "live" -> "LIVE";
            default -> "SUGGESTION";
        };
    }
}
