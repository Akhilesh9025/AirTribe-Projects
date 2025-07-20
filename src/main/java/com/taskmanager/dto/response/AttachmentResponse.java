package com.taskmanager.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {
    private Long id;
    private String filename;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private Long taskId;
    private UserResponse uploadedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}