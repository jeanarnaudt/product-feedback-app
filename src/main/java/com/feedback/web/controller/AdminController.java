package com.feedback.web.controller;

import com.feedback.repository.CategoryRepository;
import com.feedback.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final FeedbackService feedbackService;
    private final CategoryRepository categoryRepository;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("plannedCount", feedbackService.getPlanned().size());
        model.addAttribute("inProgressCount", feedbackService.getInProgress().size());
        model.addAttribute("liveCount", feedbackService.getLive().size());
        return "admin/dashboard";
    }

    @GetMapping("/feedback")
    public String feedback(Model model) {
        // For now show all non-suggestion statuses as roadmap; can be extended later
        model.addAttribute("planned", feedbackService.getPlanned());
        model.addAttribute("inProgress", feedbackService.getInProgress());
        model.addAttribute("live", feedbackService.getLive());
        return "admin/feedback";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/categories";
    }
}
