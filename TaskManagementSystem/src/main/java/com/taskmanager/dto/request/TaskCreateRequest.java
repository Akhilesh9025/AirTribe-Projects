package com.taskmanager.dto.request;

import jakarta.validation.constraints.NotBlank;
import com.taskmanager.entity.enums.Priority;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskCreateRequest {
    @NotBlank(message = "Task title is required")
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Priority priority = Priority.MEDIUM;
    private Long assignedToUserId;
}