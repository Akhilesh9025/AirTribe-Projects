package com.taskmanager.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot be longer than 100 characters")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password; // Raw password, will be hashed in service

    @Size(max = 50, message = "First name cannot be longer than 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name cannot be longer than 50 characters")
    private String lastName;
}