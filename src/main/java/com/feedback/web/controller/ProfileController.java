package com.feedback.web.controller;

import com.feedback.dto.feedback.FeedbackListItemDto;
import com.feedback.dto.user.UserProfileDto;
import com.feedback.dto.user.UserProfileUpdateRequest;
import com.feedback.service.FeedbackService;
import com.feedback.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final FeedbackService feedbackService;

    @GetMapping
    public String view(Model model) {
        UserProfileDto profile = userService.toProfileDto(userService.getCurrentUser());
        List<FeedbackListItemDto> myFeedback = feedbackService.getUserFeedback(profile.getId());
        model.addAttribute("profile", profile);
        model.addAttribute("myFeedback", myFeedback);
        return "profile/view";
    }

    @GetMapping("/edit")
    public String editForm(Model model) {
        if (!model.containsAttribute("form")) {
            UserProfileDto profile = userService.toProfileDto(userService.getCurrentUser());
            UserProfileUpdateRequest form = new UserProfileUpdateRequest();
            form.setDisplayName(profile.getDisplayName());
            form.setBio(profile.getBio());
            form.setAvatarUrl(profile.getAvatarUrl());
            model.addAttribute("form", form);
        }
        return "profile/edit";
    }

    @PostMapping("/edit")
    public String edit(@Valid @ModelAttribute("form") UserProfileUpdateRequest form,
                       BindingResult bindingResult,
                       Model model) {
        if (bindingResult.hasErrors()) {
            return "profile/edit";
        }

        userService.updateProfile(form);
        return "redirect:/profile";
    }
}
