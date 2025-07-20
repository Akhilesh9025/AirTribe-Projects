package com.taskmanager.mapper;

import com.taskmanager.dto.request.TaskCreateRequest;
import com.taskmanager.dto.request.TaskUpdateRequest;
import com.taskmanager.dto.response.TaskResponse;
import com.taskmanager.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy; // Good practice to include this

@Mapper(componentModel = "spring", uses = {UserMapper.class, ProjectMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE) // Added unmappedTargetPolicy
public interface TaskMapper {

    @Mapping(target = "id", ignore = true) // ID is generated, not from request
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true) // Set by service
    @Mapping(target = "project", ignore = true) // Set by service
    @Mapping(target = "assignedTo", ignore = true) // Set by service based on ID
    @Mapping(target = "status", ignore = true)
    Task toEntity(TaskCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateTaskFromDto(TaskUpdateRequest request, @MappingTarget Task task);

    @Mapping(source = "assignedTo", target = "assignedTo")
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "project", target = "project")
    TaskResponse toResponse(Task task);
}