package com.feedback.web.controller;

import com.feedback.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class RoadmapController {

    private final FeedbackService feedbackService;

    @GetMapping("/roadmap")
    public String roadmap(Model model) {
        model.addAttribute("planned", feedbackService.getPlanned());
        model.addAttribute("inProgress", feedbackService.getInProgress());
        model.addAttribute("live", feedbackService.getLive());

        model.addAttribute("plannedCount", feedbackService.getPlanned().size());
        model.addAttribute("inProgressCount", feedbackService.getInProgress().size());
        model.addAttribute("liveCount", feedbackService.getLive().size());
        return "roadmap/index";
    }
}
