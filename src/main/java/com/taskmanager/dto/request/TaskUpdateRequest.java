package com.taskmanager.dto.request;

import com.taskmanager.entity.enums.Priority;
import com.taskmanager.entity.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskUpdateRequest {
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private TaskStatus status;
    private Priority priority;
    private Long assignedToUserId;
}