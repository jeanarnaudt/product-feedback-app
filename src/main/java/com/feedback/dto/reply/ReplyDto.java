package com.feedback.dto.reply;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for rendering a reply to a comment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyDto {

    private Long id;
    private String content;

    private Long authorId;
    private String authorUsername;
    private String authorDisplayName;
    private String authorAvatarUrl;

    // Optional: who this reply is directed to
    private Long replyToUserId;
    private String replyToUsername;

    private Instant createdAt;
    private Instant updatedAt;
}
