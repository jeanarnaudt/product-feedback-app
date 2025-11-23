package com.feedback.dto.comment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Incoming form data for creating a new comment under a feedback item.
 */
@Data
public class CommentCreateRequest {

    @NotNull
    @Min(1)
    private Long feedbackId;

    @NotBlank
    @Size(max = 5000)
    private String content;
}
