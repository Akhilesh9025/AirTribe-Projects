package com.taskmanager.mapper;

import com.taskmanager.dto.response.AttachmentResponse;
import com.taskmanager.entity.Attachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {UserMapper.class}, // Uses UserMapper for uploadedBy
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AttachmentMapper {



    @Mapping(source = "task.id", target = "taskId")
    @Mapping(source = "uploadedBy", target = "uploadedBy")
    AttachmentResponse toResponse(Attachment attachment);
}