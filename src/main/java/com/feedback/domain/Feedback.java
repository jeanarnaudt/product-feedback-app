package com.feedback.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackStatus status = FeedbackStatus.SUGGESTION;

    @Column(name = "comment_count", nullable = false)
    private int commentCount = 0;

    @Column(name = "upvote_count", nullable = false)
    private int upvoteCount = 0;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "feedback")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Comment> comments;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    // --- Convenience counters API ---
    public void incrementCommentCount(int delta) {
        if (delta <= 0) return;
        this.commentCount = Math.max(0, this.commentCount + delta);
    }

    public void decrementCommentCount(int delta) {
        if (delta <= 0) return;
        this.commentCount = Math.max(0, this.commentCount - delta);
    }

    // --- Upvote counters API ---
    public void incrementUpvoteCount() {
        this.upvoteCount = Math.max(0, this.upvoteCount + 1);
    }

    public void decrementUpvoteCount() {
        this.upvoteCount = Math.max(0, this.upvoteCount - 1);
    }
}
