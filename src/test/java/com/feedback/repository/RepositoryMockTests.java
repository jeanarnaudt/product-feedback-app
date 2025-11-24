package com.feedback.repository;

import com.feedback.domain.Feedback;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Lightweight smoke tests for repository method signatures using Mockito.
 * This avoids DB configuration while still providing basic coverage that
 * the repository interfaces are callable as expected.
 */
class RepositoryMockTests {

    @Test
    void feedbackRepository_methods_callable() {
        FeedbackRepository repo = Mockito.mock(FeedbackRepository.class);
        when(repo.findByStatus("SUGGESTION")).thenReturn(Collections.emptyList());
        when(repo.findByAuthorId(1L)).thenReturn(Collections.emptyList());

        assertThat(repo.findByStatus("SUGGESTION")).isEmpty();
        assertThat(repo.findByAuthorId(1L)).isEmpty();
    }

    @Test
    void replyRepository_countByCommentId_callable() {
        ReplyRepository repo = Mockito.mock(ReplyRepository.class);
        when(repo.countByCommentId(999L)).thenReturn(0L);
        assertThat(repo.countByCommentId(999L)).isZero();
    }
}
