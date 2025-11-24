package com.feedback.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {
    @Size(max = 100)
    private String displayName;

    @Size(max = 500)
    private String bio;

    @Size(max = 500)
    private String avatarUrl; // For simplicity, accept URL. File upload can be added later.
}
