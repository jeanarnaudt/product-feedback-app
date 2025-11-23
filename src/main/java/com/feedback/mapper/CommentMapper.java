package com.feedback.mapper;

import com.feedback.domain.Comment;
import com.feedback.dto.comment.CommentCreateRequest;
import com.feedback.dto.comment.CommentDto;
import com.feedback.dto.comment.CommentUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = { ReplyMapper.class })
public interface CommentMapper {

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorUsername", source = "author.username")
    @Mapping(target = "authorDisplayName", source = "author.displayName")
    @Mapping(target = "authorAvatarUrl", source = "author.avatarUrl")
    CommentDto toDto(Comment entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "feedback", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Comment fromCreateRequest(CommentCreateRequest dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "feedback", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(CommentUpdateRequest dto, @MappingTarget Comment entity);
}
