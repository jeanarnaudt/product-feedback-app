package com.feedback.service;

import com.feedback.domain.Comment;
import com.feedback.domain.Feedback;
import com.feedback.domain.Reply;
import com.feedback.mapper.ReplyMapper;
import com.feedback.dto.reply.ReplyCreateRequest;
import com.feedback.dto.reply.ReplyUpdateRequest;
import com.feedback.repository.CommentRepository;
import com.feedback.repository.FeedbackRepository;
import com.feedback.repository.ReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final CommentRepository commentRepository;
    private final FeedbackRepository feedbackRepository;
    private final UserService userService;
    private final ReplyMapper replyMapper;

    /**
     * Add a reply under the given comment. Also increments the parent feedback's comment counter by 1.
     */
    @Transactional
    public void addReply(Long commentId, ReplyCreateRequest request) {
        // Load comment and related feedback
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
        Feedback feedback = comment.getFeedback();

        // Map request to entity
        Reply entity = replyMapper.fromCreateRequest(request);
        entity.setComment(comment);
        entity.setAuthor(userService.getCurrentUser());

        // Optionally resolve replyToUser
        if (request.getReplyToUserId() != null) {
            entity.setReplyToUser(userService.getByIdOrThrow(request.getReplyToUserId()));
        }

        // Persist reply
        replyRepository.save(entity);

        // Increment counter on feedback and persist
        feedback.incrementCommentCount(1);
        feedbackRepository.save(feedback);
    }

    /**
     * Update reply content.
     */
    @Transactional
    public void updateReply(Long replyId, ReplyUpdateRequest request) {
        Reply entity = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Reply not found: " + replyId));
        replyMapper.updateEntityFromRequest(request, entity);
        replyRepository.save(entity);
    }

    /**
     * Delete a reply and decrement the parent feedback's comment counter by 1.
     */
    @Transactional
    public void deleteReply(Long replyId) {
        Reply entity = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Reply not found: " + replyId));

        Feedback feedback = entity.getComment().getFeedback();

        replyRepository.delete(entity);

        feedback.decrementCommentCount(1);
        feedbackRepository.save(feedback);
    }
}
