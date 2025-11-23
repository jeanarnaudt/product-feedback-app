package com.feedback.dto.comment;

import com.feedback.dto.reply.ReplyDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * DTO for rendering a comment with its replies.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {

    private Long id;
    private String content;

    // Minimal author info for rendering
    private Long authorId;
    private String authorUsername;
    private String authorDisplayName;
    private String authorAvatarUrl;

    private Instant createdAt;
    private Instant updatedAt;

    // Nested replies for detail views
    private List<ReplyDto> replies;
}
