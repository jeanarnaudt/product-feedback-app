package com.feedback.service;

import com.feedback.domain.Category;
import com.feedback.domain.Feedback;
import com.feedback.mapper.FeedbackMapper;
import com.feedback.dto.feedback.FeedbackCreateRequest;
import com.feedback.dto.feedback.FeedbackDetailDto;
import com.feedback.dto.feedback.FeedbackListItemDto;
import com.feedback.dto.feedback.FeedbackUpdateRequest;
import com.feedback.domain.FeedbackStatus;
import com.feedback.repository.CategoryRepository;
import com.feedback.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final FeedbackMapper feedbackMapper;

    /**
     * Returns suggestion feedbacks with optional sorting and category filtering by slug.
     * Supported sort values (case-insensitive):
     * - most_upvotes (default)
     * - most_comments
     * - newest
     */
    public List<FeedbackListItemDto> getSuggestions(Optional<String> sort, Optional<String> categorySlug) {
        // Load suggestions only (status=SUGGESTION)
        List<Feedback> suggestions = feedbackRepository.findByStatus("SUGGESTION");

        // Filter by category slug if provided
        if (categorySlug != null && categorySlug.isPresent()) {
            String slug = categorySlug.get().toLowerCase(Locale.ROOT);
            suggestions = suggestions.stream()
                    .filter(f -> f.getCategory() != null && f.getCategory().getSlug() != null
                            && f.getCategory().getSlug().toLowerCase(Locale.ROOT).equals(slug))
                    .collect(Collectors.toList());
        }

        // Apply sorting
        String sortKey = sort.map(s -> s.toLowerCase(Locale.ROOT)).orElse("most_upvotes");
        Comparator<Feedback> comparator;
        switch (sortKey) {
            case "most_comments" -> comparator = Comparator.comparingInt(Feedback::getCommentCount).reversed()
                    .thenComparing(Feedback::getId);
            case "newest" -> comparator = Comparator.comparing((Feedback f) -> f.getCreatedAt(), Comparator.nullsLast(Comparator.naturalOrder())).reversed()
                    .thenComparing(Feedback::getId, Comparator.nullsLast(Comparator.naturalOrder()));
            case "most_upvotes" ->
                    comparator = Comparator.comparingInt(Feedback::getUpvoteCount).reversed()
                            .thenComparing(Feedback::getId);
            default -> comparator = Comparator.comparingInt(Feedback::getUpvoteCount).reversed()
                    .thenComparing(Feedback::getId);
        }
        suggestions.sort(comparator);

        return suggestions.stream()
                .map(feedbackMapper::toListItemDto)
                .collect(Collectors.toList());
    }

    public FeedbackDetailDto getFeedbackDetail(Long id) {
        Feedback entity = feedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found: " + id));
        return feedbackMapper.toDetailDto(entity);
    }

    public List<FeedbackListItemDto> getUserFeedback(Long userId) {
        List<Feedback> items = feedbackRepository.findByAuthorId(userId);
        return items.stream().map(feedbackMapper::toListItemDto).collect(Collectors.toList());
    }

    @Transactional
    public Long createFeedback(FeedbackCreateRequest request) {
        Feedback entity = feedbackMapper.fromCreateRequest(request);

        // Attach current user
        entity.setAuthor(userService.getCurrentUser());

        // Attach category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.getCategoryId()));
        entity.setCategory(category);

        Feedback saved = feedbackRepository.save(entity);
        return saved.getId();
    }

    public FeedbackUpdateRequest getUpdateForm(Long id) {
        Feedback entity = feedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found: " + id));

        FeedbackUpdateRequest dto = new FeedbackUpdateRequest();
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setCategoryId(entity.getCategory() != null ? entity.getCategory().getId() : null);
        return dto;
    }

    @Transactional
    public void updateFeedback(Long id, FeedbackUpdateRequest request) {
        Feedback entity = feedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found: " + id));

        // Resolve and set category if changed
        if (request.getCategoryId() != null && (entity.getCategory() == null ||
                !request.getCategoryId().equals(entity.getCategory().getId()))) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.getCategoryId()));
            entity.setCategory(category);
        }

        // Apply changes via mapper
        feedbackMapper.updateEntityFromRequest(request, entity);

        // Save
        feedbackRepository.save(entity);
    }

    @Transactional
    public void deleteFeedback(Long id) {
        if (!feedbackRepository.existsById(id)) {
            // no-op if not found
            return;
        }
        feedbackRepository.deleteById(id);
    }

    /**
     * Returns feedback items for the roadmap grouped by status. Convenience getters provided
     * for Thymeleaf binding.
     */
    public List<FeedbackListItemDto> getPlanned() {
        return getByStatusSorted(FeedbackStatus.PLANNED);
    }

    public List<FeedbackListItemDto> getInProgress() {
        return getByStatusSorted(FeedbackStatus.IN_PROGRESS);
    }

    public List<FeedbackListItemDto> getLive() {
        return getByStatusSorted(FeedbackStatus.LIVE);
    }

    private List<FeedbackListItemDto> getByStatusSorted(FeedbackStatus status) {
        List<Feedback> items = feedbackRepository.findByStatus(status.name());
        // Sort by upvotes desc, then comments desc, then id
        items.sort(Comparator
                .comparingInt(Feedback::getUpvoteCount).reversed()
                .thenComparingInt(Feedback::getCommentCount).reversed()
                .thenComparing(Feedback::getId));
        return items.stream().map(feedbackMapper::toListItemDto).collect(Collectors.toList());
    }
}
