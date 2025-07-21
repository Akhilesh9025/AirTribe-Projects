package com.taskmanager.mapper;

import com.taskmanager.dto.request.ProjectCreateRequest;
import com.taskmanager.dto.request.ProjectUpdateRequest;
import com.taskmanager.dto.response.ProjectResponse;
import com.taskmanager.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {UserMapper.class, TeamMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProjectMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "team", ignore = true) // Set in service from path variable
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    Project toEntity(ProjectCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    void updateProjectFromDto(ProjectUpdateRequest dto, @MappingTarget Project entity);

    @Mapping(source = "creator", target = "creator")
    @Mapping(source = "team", target = "team")
    @Mapping(source = "members", target = "members")
    @Mapping(target = "tasks", ignore = true) // Avoid infinite recursion
    ProjectResponse toResponse(Project project);
}