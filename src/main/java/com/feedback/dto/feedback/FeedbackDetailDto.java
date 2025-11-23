package com.feedback.dto.feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Detailed view of a feedback item suitable for a detail page.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackDetailDto {

    private Long id;
    private String title;
    private String description;
    /**
     * Feedback status represented as a string (e.g., "SUGGESTION", "PLANNED").
     */
    private String status;
    private String categoryName;
    private int upvoteCount;
    private int commentCount;

    // Minimal author info for rendering
    private Long authorId;
    private String authorUsername;
    private String authorDisplayName;
}
