package com.feedback.service;

import com.feedback.domain.Comment;
import com.feedback.domain.Feedback;
import com.feedback.mapper.CommentMapper;
import com.feedback.dto.comment.CommentCreateRequest;
import com.feedback.dto.comment.CommentUpdateRequest;
import com.feedback.repository.CommentRepository;
import com.feedback.repository.FeedbackRepository;
import com.feedback.repository.ReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final FeedbackRepository feedbackRepository;
    private final ReplyRepository replyRepository;
    private final UserService userService;
    private final CommentMapper commentMapper;

    @Transactional
    public void addComment(Long feedbackId, CommentCreateRequest request) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found: " + feedbackId));

        Comment entity = commentMapper.fromCreateRequest(request);
        entity.setAuthor(userService.getCurrentUser());
        entity.setFeedback(feedback);

        commentRepository.save(entity);

        // increment comment counter on feedback
        feedback.incrementCommentCount(1);
        // persist updated counter
        feedbackRepository.save(feedback);
    }

    @Transactional
    public void updateComment(Long commentId, CommentUpdateRequest request) {
        Comment entity = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));

        commentMapper.updateEntityFromRequest(request, entity);
        commentRepository.save(entity);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment entity = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));

        Feedback feedback = entity.getFeedback();

        long replyCount = replyRepository.countByCommentId(commentId);

        // If cascade is not configured, uncomment the next line to delete replies explicitly
        // replyRepository.deleteByCommentId(commentId);

        commentRepository.delete(entity);

        feedback.decrementCommentCount((int) (1 + replyCount));
        feedbackRepository.save(feedback);
    }
}
