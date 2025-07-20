package com.taskmanager.mapper;

import com.taskmanager.dto.request.TeamCreateRequest;
import com.taskmanager.dto.request.TeamUpdateRequest;
import com.taskmanager.dto.response.TeamResponse;
import com.taskmanager.entity.Team;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {UserMapper.class}, // Uses UserMapper for members and creator
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TeamMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "creator", ignore = true) // Set in service
    @Mapping(target = "members", ignore = true) // Handled in service
    @Mapping(target = "projects", ignore = true) // Avoid infinite recursion or large payloads
    Team toEntity(TeamCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "projects", ignore = true)
    void updateTeamFromDto(TeamUpdateRequest dto, @MappingTarget Team entity);

    @Mapping(source = "creator", target = "creator")
    @Mapping(source = "members", target = "members")
    @Mapping(target = "projects", ignore = true) // Avoid infinite recursion or large payloads
    TeamResponse toResponse(Team team);
}