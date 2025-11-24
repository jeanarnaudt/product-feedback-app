package com.feedback.service;

import com.feedback.domain.User;
import com.feedback.dto.user.UserProfileDto;
import com.feedback.dto.user.UserProfileUpdateRequest;
import com.feedback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Returns the currently authenticated User entity from the SecurityContext.
     * Throws IllegalStateException if no authenticated principal is available.
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new IllegalStateException("No authenticated user in security context");
        }

        String username = auth.getName();
        return getByUsernameOrThrow(username);
    }

    /**
     * Register a new user. Performs uniqueness checks for username and email and hashes the password.
     */
    @Transactional
    public User registerUser(String username, String email, String rawPassword, String displayName) {
        userRepository.findByUsername(username).ifPresent(u -> {
            throw new IllegalArgumentException("Username already exists");
        });
        userRepository.findByEmail(email).ifPresent(u -> {
            throw new IllegalArgumentException("Email already exists");
        });

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .displayName(displayName)
                .role("USER")
                .enabled(true)
                .build();

        return userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User id not found: " + id));
    }

    public User getByUsernameOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + username));
    }

    public UserProfileDto toProfileDto(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    @Transactional
    public void updateProfile(UserProfileUpdateRequest request) {
        User current = getCurrentUser();
        current.setDisplayName(request.getDisplayName());
        current.setBio(request.getBio());
        current.setAvatarUrl(request.getAvatarUrl());
        userRepository.save(current);
    }
}
