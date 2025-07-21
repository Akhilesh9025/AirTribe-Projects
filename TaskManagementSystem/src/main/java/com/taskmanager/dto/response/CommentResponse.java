package com.taskmanager.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private Long taskId;
    private UserResponse author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}