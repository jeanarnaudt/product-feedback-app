package com.feedback.service;

import com.feedback.domain.Feedback;
import com.feedback.domain.Upvote;
import com.feedback.domain.UpvoteId;
import com.feedback.domain.User;
import com.feedback.repository.FeedbackRepository;
import com.feedback.repository.UpvoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpvoteService {

    private final UpvoteRepository upvoteRepository;
    private final FeedbackRepository feedbackRepository;
    private final UserService userService;

    /**
     * Toggle upvote for the current user on the given feedback.
     * - If an upvote exists: remove it and decrement counter.
     * - If it doesn't: create it and increment counter.
     */
    @Transactional
    public void toggleUpvote(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found: " + feedbackId));

        User current = userService.getCurrentUser();

        upvoteRepository.findByUserIdAndFeedbackId(current.getId(), feedback.getId())
                .ifPresentOrElse(existing -> {
                    // Remove existing upvote
                    upvoteRepository.delete(existing);
                    feedback.decrementUpvoteCount();
                }, () -> {
                    // Create new upvote
                    Upvote upvote = Upvote.builder()
                            .id(new UpvoteId(current.getId(), feedback.getId()))
                            .user(current)
                            .feedback(feedback)
                            .build();
                    upvoteRepository.save(upvote);
                    feedback.incrementUpvoteCount();
                });

        // Persist feedback counter change
        feedbackRepository.save(feedback);
    }
}
