package com.taskmanager.dto.response;

import com.taskmanager.entity.enums.Priority;
import com.taskmanager.entity.enums.TaskStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private LocalDateTime dueDate;
    private UserResponse assignedTo;
    private UserResponse createdBy;
    private ProjectResponse project;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}