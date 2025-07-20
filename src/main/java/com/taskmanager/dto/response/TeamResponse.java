// src/main/java/com/taskmanager/dto/response/TeamResponse.java
package com.taskmanager.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List; // For projects
import java.util.Set; // For members

@Data
public class TeamResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserResponse creator;
    private Set<UserResponse> members;

    private List<ProjectResponse> projects;
}