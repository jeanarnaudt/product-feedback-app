package com.feedback.web.controller;

import com.feedback.dto.reply.ReplyCreateRequest;
import com.feedback.dto.reply.ReplyUpdateRequest;
import com.feedback.service.ReplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;

    // POST /comments/{commentId}/replies → create reply under a comment
    @PostMapping("/comments/{commentId}/replies")
    public String addReply(@PathVariable Long commentId,
                           @RequestParam("feedbackId") Long feedbackId,
                           @Valid @ModelAttribute("reply") ReplyCreateRequest request,
                           BindingResult bindingResult) {
        // Ensure DTO has the correct commentId from path
        request.setCommentId(commentId);

        if (bindingResult.hasErrors()) {
            return "redirect:/feedback/" + feedbackId;
        }

        replyService.addReply(commentId, request);
        return "redirect:/feedback/" + feedbackId;
    }

    // POST /replies/{replyId}/edit → update reply content
    @PostMapping("/replies/{replyId}/edit")
    public String editReply(@PathVariable Long replyId,
                            @RequestParam("feedbackId") Long feedbackId,
                            @Valid @ModelAttribute("reply") ReplyUpdateRequest request,
                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/feedback/" + feedbackId;
        }
        replyService.updateReply(replyId, request);
        return "redirect:/feedback/" + feedbackId;
    }

    // POST /replies/{replyId}/delete → delete a reply
    @PostMapping("/replies/{replyId}/delete")
    public String deleteReply(@PathVariable Long replyId,
                              @RequestParam("feedbackId") Long feedbackId) {
        replyService.deleteReply(replyId);
        return "redirect:/feedback/" + feedbackId;
    }
}
