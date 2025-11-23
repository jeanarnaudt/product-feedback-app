package com.feedback.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class UpvoteId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "feedback_id")
    private Long feedbackId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpvoteId upvoteId = (UpvoteId) o;
        return Objects.equals(userId, upvoteId.userId) && Objects.equals(feedbackId, upvoteId.feedbackId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, feedbackId);
    }
}
