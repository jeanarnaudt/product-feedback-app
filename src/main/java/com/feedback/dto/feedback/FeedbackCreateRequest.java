package com.feedback.dto.feedback;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Incoming form data for creating a new feedback.
 */
@Data
public class FeedbackCreateRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    private String description;

    @NotNull
    @Min(1)
    private Long categoryId;
}
