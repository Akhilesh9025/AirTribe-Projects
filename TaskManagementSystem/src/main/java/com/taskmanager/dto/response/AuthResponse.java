package com.taskmanager.dto.response;

import com.taskmanager.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private Long id;
    private String username;
    private String email;
    private Set<UserRole> roles;

}