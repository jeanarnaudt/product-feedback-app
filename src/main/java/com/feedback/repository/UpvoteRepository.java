package com.feedback.repository;

import com.feedback.domain.Upvote;
import com.feedback.domain.UpvoteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UpvoteRepository extends JpaRepository<Upvote, UpvoteId> {

    @Query("select u from Upvote u where u.user.id = ?1 and u.feedback.id = ?2")
    Optional<Upvote> findByUserIdAndFeedbackId(Long userId, Long feedbackId);
}
