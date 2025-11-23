package com.feedback.dto.reply;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Incoming form data for creating a new reply under a comment.
 */
@Data
public class ReplyCreateRequest {

    @NotNull
    @Min(1)
    private Long commentId;

    // Optional: reply directed to a specific user
    private Long replyToUserId;

    @NotBlank
    @Size(max = 5000)
    private String content;
}
