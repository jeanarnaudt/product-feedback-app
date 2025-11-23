package com.feedback.mapper;

import com.feedback.domain.Feedback;
import com.feedback.dto.feedback.FeedbackCreateRequest;
import com.feedback.dto.feedback.FeedbackDetailDto;
import com.feedback.dto.feedback.FeedbackListItemDto;
import com.feedback.dto.feedback.FeedbackUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "status", expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)")
    FeedbackListItemDto toListItemDto(Feedback entity);

    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "status", expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorUsername", source = "author.username")
    @Mapping(target = "authorDisplayName", source = "author.displayName")
    FeedbackDetailDto toDetailDto(Feedback entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "SUGGESTION")
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "upvoteCount", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Feedback fromCreateRequest(FeedbackCreateRequest dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "upvoteCount", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(FeedbackUpdateRequest dto, @MappingTarget Feedback entity);
}
