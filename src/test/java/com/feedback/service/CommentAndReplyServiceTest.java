package com.feedback.service;

import com.feedback.domain.Comment;
import com.feedback.domain.Feedback;
import com.feedback.domain.Reply;
import com.feedback.dto.comment.CommentCreateRequest;
import com.feedback.dto.comment.CommentUpdateRequest;
import com.feedback.dto.reply.ReplyCreateRequest;
import com.feedback.dto.reply.ReplyUpdateRequest;
import com.feedback.mapper.CommentMapper;
import com.feedback.mapper.ReplyMapper;
import com.feedback.repository.CommentRepository;
import com.feedback.repository.FeedbackRepository;
import com.feedback.repository.ReplyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentAndReplyServiceTest {

    // CommentService wiring
    @Mock private CommentRepository commentRepository;
    @Mock private FeedbackRepository feedbackRepository;
    @Mock private ReplyRepository replyRepository;
    @Mock private UserService userService;
    @Mock private CommentMapper commentMapper;
    @InjectMocks private CommentService commentService;

    // ReplyService wiring
    @Mock private ReplyMapper replyMapper;
    @InjectMocks private ReplyService replyService;

    @Test
    void addComment_incrementsFeedbackCommentCount() {
        Feedback fb = new Feedback();
        fb.setId(1L);
        fb.setCommentCount(0);

        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(fb));

        CommentCreateRequest req = new CommentCreateRequest();
        req.setContent("Hello");

        Comment mapped = new Comment();
        when(commentMapper.fromCreateRequest(req)).thenReturn(mapped);

        when(userService.getCurrentUser()).thenReturn(new com.feedback.domain.User());

        commentService.addComment(1L, req);

        assertThat(fb.getCommentCount()).isEqualTo(1);
        verify(feedbackRepository).save(fb);
        verify(commentRepository).save(mapped);
    }

    @Test
    void deleteComment_decrementsByOnePlusReplies() {
        Feedback fb = new Feedback(); fb.setId(1L); fb.setCommentCount(10);
        Comment c = new Comment(); c.setId(5L); c.setFeedback(fb);

        when(commentRepository.findById(5L)).thenReturn(Optional.of(c));
        when(replyRepository.countByCommentId(5L)).thenReturn(3L);

        commentService.deleteComment(5L);

        assertThat(fb.getCommentCount()).isEqualTo(6); // 10 - (1 + 3)
        verify(commentRepository).delete(c);
        verify(feedbackRepository).save(fb);
    }

    @Test
    void updateComment_updatesRepository() {
        Comment c = new Comment(); c.setId(7L);
        when(commentRepository.findById(7L)).thenReturn(Optional.of(c));

        CommentUpdateRequest req = new CommentUpdateRequest();
        req.setContent("new");

        commentService.updateComment(7L, req);
        verify(commentMapper).updateEntityFromRequest(req, c);
        verify(commentRepository).save(c);
    }

    @Test
    void addReply_incrementsFeedbackCommentCount() {
        Feedback fb = new Feedback(); fb.setId(1L); fb.setCommentCount(2);
        Comment c = new Comment(); c.setId(3L); c.setFeedback(fb);
        when(commentRepository.findById(3L)).thenReturn(Optional.of(c));

        ReplyCreateRequest req = new ReplyCreateRequest();
        req.setContent("hi");

        Reply mapped = new Reply();
        when(replyMapper.fromCreateRequest(req)).thenReturn(mapped);
        when(userService.getCurrentUser()).thenReturn(new com.feedback.domain.User());

        replyService.addReply(3L, req);

        assertThat(fb.getCommentCount()).isEqualTo(3);
        verify(replyRepository).save(mapped);
        verify(feedbackRepository).save(fb);
    }

    @Test
    void deleteReply_decrementsFeedbackCommentCount() {
        Feedback fb = new Feedback(); fb.setId(1L); fb.setCommentCount(4);
        Comment c = new Comment(); c.setId(3L); c.setFeedback(fb);
        Reply r = new Reply(); r.setId(9L); r.setComment(c);

        when(replyRepository.findById(9L)).thenReturn(Optional.of(r));

        replyService.deleteReply(9L);

        assertThat(fb.getCommentCount()).isEqualTo(3);
        verify(replyRepository).delete(r);
        verify(feedbackRepository).save(fb);
    }
}
