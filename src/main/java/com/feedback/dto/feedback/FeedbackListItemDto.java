package com.feedback.dto.feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal data needed to render a feedback item in list views.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackListItemDto {

    private Long id;
    private String title;
    /**
     * Human-readable category name (e.g., "Feature", "UI").
     */
    private String categoryName;
    /**
     * Feedback status represented as a string (e.g., "SUGGESTION", "PLANNED").
     */
    private String status;
    private int upvoteCount;
    private int commentCount;
}
