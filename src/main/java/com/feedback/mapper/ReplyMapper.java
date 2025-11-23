package com.feedback.mapper;

import com.feedback.domain.Reply;
import com.feedback.dto.reply.ReplyCreateRequest;
import com.feedback.dto.reply.ReplyDto;
import com.feedback.dto.reply.ReplyUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ReplyMapper {

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorUsername", source = "author.username")
    @Mapping(target = "authorDisplayName", source = "author.displayName")
    @Mapping(target = "authorAvatarUrl", source = "author.avatarUrl")
    @Mapping(target = "replyToUserId", source = "replyToUser.id")
    @Mapping(target = "replyToUsername", source = "replyToUser.username")
    ReplyDto toDto(Reply entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "comment", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "replyToUser", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Reply fromCreateRequest(ReplyCreateRequest dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "comment", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "replyToUser", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(ReplyUpdateRequest dto, @MappingTarget Reply entity);
}
