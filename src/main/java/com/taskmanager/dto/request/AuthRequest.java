package com.taskmanager.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {
    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail; // Can be username or email for login

    @NotBlank(message = "Password is required")
    private String password;
}