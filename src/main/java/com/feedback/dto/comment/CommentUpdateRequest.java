package com.feedback.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Incoming form data for updating an existing comment's content.
 */
@Data
public class CommentUpdateRequest {

    @NotBlank
    @Size(max = 5000)
    private String content;
}
