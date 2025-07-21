package com.taskmanager.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TeamUpdateRequest {
    @Size(min = 3, max = 100, message = "Team name must be between 3 and 100 characters")
    private String name;
    private String description;
}