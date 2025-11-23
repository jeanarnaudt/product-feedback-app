package com.feedback.web.controller;

import com.feedback.dto.feedback.FeedbackCreateRequest;
import com.feedback.dto.feedback.FeedbackUpdateRequest;
import com.feedback.service.FeedbackService;
import com.feedback.service.UpvoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UpvoteService upvoteService;

    // GET /feedback → list suggestions
    @GetMapping
    public String listFeedback(
            @RequestParam(name = "sort", required = false) Optional<String> sort,
            @RequestParam(name = "category", required = false) Optional<String> category,
            Model model) {
        model.addAttribute("feedback", feedbackService.getSuggestions(sort, category));
        return "feedback/list";
    }

    // GET /feedback/{id} → detail
    @GetMapping("/{id}")
    public String feedbackDetail(@PathVariable Long id, Model model) {
        model.addAttribute("feedback", feedbackService.getFeedbackDetail(id));
        return "feedback/detail";
    }

    // GET /feedback/new → show create form
    @GetMapping("/new")
    public String newFeedbackForm(Model model) {
        if (!model.containsAttribute("feedback")) {
            model.addAttribute("feedback", new FeedbackCreateRequest());
        }
        return "feedback/new";
    }

    // POST /feedback → create
    @PostMapping
    public String createFeedback(@Valid @ModelAttribute("feedback") FeedbackCreateRequest request,
                                 BindingResult bindingResult,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            return "feedback/new";
        }
        Long id = feedbackService.createFeedback(request);
        return "redirect:/feedback/" + id;
    }

    // GET /feedback/{id}/edit → edit form
    @GetMapping("/{id}/edit")
    public String editFeedbackForm(@PathVariable Long id, Model model) {
        FeedbackUpdateRequest form = feedbackService.getUpdateForm(id);
        model.addAttribute("feedback", form);
        model.addAttribute("feedbackId", id);
        return "feedback/edit";
    }

    // POST /feedback/{id} → update
    @PostMapping("/{id}")
    public String updateFeedback(@PathVariable Long id,
                                 @Valid @ModelAttribute("feedback") FeedbackUpdateRequest request,
                                 BindingResult bindingResult,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("feedbackId", id);
            return "feedback/edit";
        }
        feedbackService.updateFeedback(id, request);
        return "redirect:/feedback/" + id;
    }

    // POST /feedback/{id}/delete → delete
    @PostMapping("/{id}/delete")
    public String deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return "redirect:/feedback";
    }

    // POST /feedback/{id}/upvote → toggle upvote and redirect back to detail
    @PostMapping("/{id}/upvote")
    public String toggleUpvote(@PathVariable Long id) {
        upvoteService.toggleUpvote(id);
        return "redirect:/feedback/" + id;
    }
}
