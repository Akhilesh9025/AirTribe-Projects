package com.taskmanager.mapper;

import com.taskmanager.dto.request.CommentCreateRequest;
import com.taskmanager.dto.request.CommentUpdateRequest;
import com.taskmanager.dto.response.CommentResponse;
import com.taskmanager.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {UserMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "author", ignore = true) // Set in service
    Comment toEntity(CommentCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "author", ignore = true)
    void updateCommentFromDto(CommentUpdateRequest dto, @MappingTarget Comment entity);

    @Mapping(source = "task.id", target = "taskId") // Map only task ID for response
    @Mapping(source = "author", target = "author")
    CommentResponse toResponse(Comment comment);
}