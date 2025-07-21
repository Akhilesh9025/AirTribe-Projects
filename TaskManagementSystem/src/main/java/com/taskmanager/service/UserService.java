package com.taskmanager.service;

import com.taskmanager.dto.request.UserUpdateRequest;
import com.taskmanager.dto.response.UserResponse;
import com.taskmanager.entity.User;
import com.taskmanager.entity.enums.UserRole;
import com.taskmanager.exception.BadRequestException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.mapper.UserMapper;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    private User getCurrentAuthenticatedUserEntity() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));
        }
        throw new BadRequestException("User not authenticated.");
    }

    public UserResponse getCurrentUser() {
        User currentUser = getCurrentAuthenticatedUserEntity();
        return userMapper.toResponse(currentUser);
    }

    @Transactional
    public UserResponse updateCurrentUser(UserUpdateRequest request) {
        User currentUser = getCurrentAuthenticatedUserEntity();

        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            // Check if username is already taken by another user
            Optional<User> existingUserByUsername = userRepository.findByUsername(request.getUsername());
            if (existingUserByUsername.isPresent() && !existingUserByUsername.get().getId().equals(currentUser.getId())) {
                throw new BadRequestException("Username '" + request.getUsername() + "' is already taken.");
            }
            currentUser.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            // Check if email is already taken by another user
            Optional<User> existingUserByEmail = userRepository.findByEmail(request.getEmail());
            if (existingUserByEmail.isPresent() && !existingUserByEmail.get().getId().equals(currentUser.getId())) {
                throw new BadRequestException("Email '" + request.getEmail() + "' is already taken.");
            }
            currentUser.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            currentUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getFirstName() != null) {
            currentUser.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            currentUser.setLastName(request.getLastName());
        }

        User updatedUser = userRepository.save(currentUser);
        return userMapper.toResponse(updatedUser);
    }

    public UserResponse getUserById(Long id) {
        User currentUser = getCurrentAuthenticatedUserEntity(); // Get current user for authorization check

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (!currentUser.getId().equals(id) && !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to view this user's profile.");
        }

        return userMapper.toResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(Long id) {
        User currentUser = getCurrentAuthenticatedUserEntity(); // Get current user for authorization check

        User userToDelete = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (!currentUser.getRoles().contains(UserRole.ROLE_ADMIN) && !currentUser.getId().equals(id)) {
            throw new BadRequestException("You are not authorized to delete this user.");
        }
        if (currentUser.getId().equals(id)) {
            throw new BadRequestException("You cannot delete your own account. Please contact an admin for assistance.");
        }

        userRepository.delete(userToDelete);
    }
}