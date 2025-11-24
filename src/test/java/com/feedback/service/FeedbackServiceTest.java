package com.feedback.service;

import com.feedback.domain.Category;
import com.feedback.domain.Feedback;
import com.feedback.domain.FeedbackStatus;
import com.feedback.dto.feedback.FeedbackCreateRequest;
import com.feedback.dto.feedback.FeedbackDetailDto;
import com.feedback.dto.feedback.FeedbackListItemDto;
import com.feedback.dto.feedback.FeedbackUpdateRequest;
import com.feedback.mapper.FeedbackMapper;
import com.feedback.repository.CategoryRepository;
import com.feedback.repository.FeedbackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock private FeedbackRepository feedbackRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private UserService userService;
    @Mock private FeedbackMapper feedbackMapper;

    @InjectMocks private FeedbackService service;

    private Feedback fb(long id, int upvotes, int comments, String categorySlug, Instant createdAt) {
        Feedback f = Feedback.builder()
                .id(id)
                .title("t" + id)
                .description("d" + id)
                .status(FeedbackStatus.SUGGESTION)
                .upvoteCount(upvotes)
                .commentCount(comments)
                .build();
        Category c = new Category();
        c.setId(id);
        c.setName("Cat" + id);
        c.setSlug(categorySlug);
        f.setCategory(c);
        // createdAt is managed by DB, but our comparator tolerates nulls; keep null or reflect via reflection if needed
        return f;
    }

    @BeforeEach
    void setup() {
    }

    @Test
    void getSuggestions_sortsByMostUpvotesByDefault_andFiltersByCategory() {
        Feedback f1 = fb(1, 5, 2, "ui", null);
        Feedback f2 = fb(2, 10, 0, "ux", null);
        Feedback f3 = fb(3, 7, 5, "ui", null);

        when(feedbackRepository.findByStatus("SUGGESTION")).thenReturn(Arrays.asList(f1, f2, f3));

        when(feedbackMapper.toListItemDto(any())).thenAnswer(inv -> {
            Feedback e = inv.getArgument(0);
            FeedbackListItemDto dto = new FeedbackListItemDto();
            dto.setId(e.getId());
            dto.setTitle(e.getTitle());
            dto.setUpvoteCount(e.getUpvoteCount());
            dto.setCommentCount(e.getCommentCount());
            return dto;
        });

        List<FeedbackListItemDto> filtered = service.getSuggestions(Optional.empty(), Optional.of("ui"));

        assertThat(filtered).hasSize(2);
        assertThat(filtered.get(0).getId()).isEqualTo(3L); // 7 upvotes vs 5
        assertThat(filtered.get(1).getId()).isEqualTo(1L);
    }

    @Test
    void getSuggestions_sortsByMostComments() {
        Feedback f1 = fb(1, 1, 2, "ui", null);
        Feedback f2 = fb(2, 1, 5, "ui", null);
        when(feedbackRepository.findByStatus("SUGGESTION")).thenReturn(Arrays.asList(f1, f2));
        when(feedbackMapper.toListItemDto(any())).thenReturn(new FeedbackListItemDto());

        List<FeedbackListItemDto> list = service.getSuggestions(Optional.of("most_comments"), Optional.empty());
        // We can't directly access order from DTOs without ids unless mapping preserves; adjust mapping to capture ids
        // Re-map to preserve order via captor
        ArgumentCaptor<Feedback> captor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackMapper, times(2)).toListItemDto(captor.capture());
        List<Feedback> ordered = captor.getAllValues();
        assertThat(ordered.get(0).getCommentCount()).isGreaterThanOrEqualTo(ordered.get(1).getCommentCount());
    }

    @Test
    void getFeedbackDetail_mapsToDto_orThrows() {
        Feedback f = fb(10, 0, 0, "ui", null);
        when(feedbackRepository.findById(10L)).thenReturn(Optional.of(f));
        FeedbackDetailDto expected = new FeedbackDetailDto();
        when(feedbackMapper.toDetailDto(f)).thenReturn(expected);

        FeedbackDetailDto dto = service.getFeedbackDetail(10L);
        assertThat(dto).isSameAs(expected);

        when(feedbackRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.getFeedbackDetail(99L));
    }

    @Test
    void createFeedback_attachesAuthorAndCategory_andReturnsId() {
        FeedbackCreateRequest req = new FeedbackCreateRequest();
        req.setTitle("Title");
        req.setDescription("Desc");
        req.setCategoryId(5L);

        Feedback mapped = new Feedback();
        when(feedbackMapper.fromCreateRequest(req)).thenReturn(mapped);

        Category cat = new Category();
        cat.setId(5L);
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(cat));

        when(userService.getCurrentUser()).thenReturn(new com.feedback.domain.User());

        Feedback saved = new Feedback();
        saved.setId(123L);
        when(feedbackRepository.save(mapped)).thenReturn(saved);

        Long id = service.createFeedback(req);

        assertThat(id).isEqualTo(123L);
        verify(feedbackRepository).save(mapped);
        assertThat(mapped.getCategory()).isSameAs(cat);
        assertThat(mapped.getAuthor()).isNotNull();
    }

    @Test
    void updateFeedback_resolvesCategoryAndSaves() {
        Feedback existing = new Feedback();
        existing.setId(7L);
        Category old = new Category(); old.setId(1L); existing.setCategory(old);
        when(feedbackRepository.findById(7L)).thenReturn(Optional.of(existing));

        FeedbackUpdateRequest req = new FeedbackUpdateRequest();
        req.setTitle("New");
        req.setDescription("X");
        req.setCategoryId(2L);
        req.setStatus(FeedbackStatus.IN_PROGRESS);

        Category newCat = new Category(); newCat.setId(2L);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCat));

        service.updateFeedback(7L, req);

        verify(feedbackMapper).updateEntityFromRequest(req, existing);
        verify(feedbackRepository).save(existing);
        assertThat(existing.getCategory()).isSameAs(newCat);
    }

    @Test
    void deleteFeedback_noopIfNotExists_elseDeletes() {
        when(feedbackRepository.existsById(1L)).thenReturn(false);
        service.deleteFeedback(1L);
        verify(feedbackRepository, never()).deleteById(any());

        when(feedbackRepository.existsById(2L)).thenReturn(true);
        service.deleteFeedback(2L);
        verify(feedbackRepository).deleteById(2L);
    }
}
