package com.feedback.web.controller;

import com.feedback.dto.comment.CommentCreateRequest;
import com.feedback.dto.comment.CommentUpdateRequest;
import com.feedback.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // POST /feedback/{feedbackId}/comments → create comment
    @PostMapping("/feedback/{feedbackId}/comments")
    public String addComment(@PathVariable Long feedbackId,
                             @Valid @ModelAttribute("comment") CommentCreateRequest request,
                             BindingResult bindingResult) {
        // Ensure DTO has the correct feedbackId (form may send it as hidden field)
        request.setFeedbackId(feedbackId);

        if (bindingResult.hasErrors()) {
            // Minimal handling: redirect back to detail page
            return "redirect:/feedback/" + feedbackId;
        }
        commentService.addComment(feedbackId, request);
        return "redirect:/feedback/" + feedbackId;
    }

    // POST /comments/{commentId}/edit → update comment content
    @PostMapping("/comments/{commentId}/edit")
    public String editComment(@PathVariable Long commentId,
                              @RequestParam("feedbackId") Long feedbackId,
                              @Valid @ModelAttribute("comment") CommentUpdateRequest request,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/feedback/" + feedbackId;
        }
        commentService.updateComment(commentId, request);
        return "redirect:/feedback/" + feedbackId;
    }

    // POST /comments/{commentId}/delete → delete a comment
    @PostMapping("/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long commentId,
                                @RequestParam("feedbackId") Long feedbackId) {
        commentService.deleteComment(commentId);
        return "redirect:/feedback/" + feedbackId;
    }
}
