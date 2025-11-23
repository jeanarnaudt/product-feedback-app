package com.feedback.dto.reply;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Incoming form data for updating an existing reply's content.
 */
@Data
public class ReplyUpdateRequest {

    @NotBlank
    @Size(max = 5000)
    private String content;
}
