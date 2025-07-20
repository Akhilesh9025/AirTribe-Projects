// src/main/java/com/taskmanager/dto/response/ProjectResponse.java
package com.taskmanager.dto.response;

import com.taskmanager.entity.enums.TaskStatus; // If TaskResponse uses it

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List; // For tasks
import java.util.Set; // For members if applicable

@Data
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserResponse creator;
    private TeamResponse team;
    private Set<UserResponse> members; // Project members

    private List<TaskResponse> tasks;
}