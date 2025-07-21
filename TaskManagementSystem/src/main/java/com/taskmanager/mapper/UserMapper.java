package com.taskmanager.mapper;

import com.taskmanager.dto.response.UserResponse;
import com.taskmanager.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping; // Import Mapping
import org.mapstruct.ReportingPolicy; // Import ReportingPolicy

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    UserResponse toResponse(User user);
}