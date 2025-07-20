package com.taskmanager.controller;

import com.taskmanager.dto.request.TaskCreateRequest;
import com.taskmanager.dto.request.TaskUpdateRequest;
import com.taskmanager.dto.response.TaskResponse;
import com.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams/{teamId}/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<TaskResponse> createTask(@PathVariable Long teamId,
                                                   @PathVariable Long projectId,
                                                   @Valid @RequestBody TaskCreateRequest request) {
        TaskResponse task = taskService.createTask(teamId, projectId, request);
        return new ResponseEntity<>(task, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<TaskResponse>> getTasks(@PathVariable Long teamId,
                                                       @PathVariable Long projectId,
                                                       @RequestParam(required = false) String status,
                                                       @RequestParam(required = false) String assignedTo,
                                                       @RequestParam(required = false) String search,
                                                       @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                                                       @RequestParam(required = false, defaultValue = "desc") String sortDir) {
        List<TaskResponse> tasks = taskService.getTasks(teamId, projectId, status, assignedTo, search, sortBy, sortDir);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long teamId,
                                                    @PathVariable Long projectId,
                                                    @PathVariable Long taskId) {
        TaskResponse task = taskService.getTaskById(teamId, projectId, taskId);
        return ResponseEntity.ok(task);
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long teamId,
                                                   @PathVariable Long projectId,
                                                   @PathVariable Long taskId,
                                                   @Valid @RequestBody TaskUpdateRequest request) {
        TaskResponse updatedTask = taskService.updateTask(teamId, projectId, taskId, request);
        return ResponseEntity.ok(updatedTask);
    }

    @PatchMapping("/{taskId}/status")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<TaskResponse> updateTaskStatus(@PathVariable Long teamId,
                                                         @PathVariable Long projectId,
                                                         @PathVariable Long taskId,
                                                         @RequestParam String newStatus) {
        TaskUpdateRequest request = new TaskUpdateRequest();
        request.setStatus(com.taskmanager.entity.enums.TaskStatus.valueOf(newStatus.toUpperCase()));
        TaskResponse updatedTask = taskService.updateTask(teamId, projectId, taskId, request);
        return ResponseEntity.ok(updatedTask);
    }

    @PatchMapping("/{taskId}/assign")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<TaskResponse> assignTask(@PathVariable Long teamId,
                                                   @PathVariable Long projectId,
                                                   @PathVariable Long taskId,
                                                   @RequestParam(required = false) Long userId) {
        TaskUpdateRequest request = new TaskUpdateRequest();
        request.setAssignedToUserId(userId);
        TaskResponse updatedTask = taskService.updateTask(teamId, projectId, taskId, request);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public void deleteTask(@PathVariable Long teamId,
                           @PathVariable Long projectId,
                           @PathVariable Long taskId) {
        taskService.deleteTask(teamId, projectId, taskId);
    }
}