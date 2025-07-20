package com.taskmanager.controller;

import com.taskmanager.dto.request.ProjectCreateRequest;
import com.taskmanager.dto.request.ProjectUpdateRequest;
import com.taskmanager.dto.response.ProjectResponse;
import com.taskmanager.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams/{teamId}/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<ProjectResponse> createProject(@PathVariable Long teamId,
                                                         @Valid @RequestBody ProjectCreateRequest request) {
        ProjectResponse project = projectService.createProject(teamId, request);
        return new ResponseEntity<>(project, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<ProjectResponse>> getProjectsByTeam(@PathVariable Long teamId) {
        List<ProjectResponse> projects = projectService.getProjectsByTeam(teamId);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long teamId, @PathVariable Long projectId) {
        ProjectResponse project = projectService.getProjectById(teamId, projectId);
        return ResponseEntity.ok(project);
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long teamId,
                                                         @PathVariable Long projectId,
                                                         @Valid @RequestBody ProjectUpdateRequest request) {
        ProjectResponse updatedProject = projectService.updateProject(teamId, projectId, request);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public void deleteProject(@PathVariable Long teamId, @PathVariable Long projectId) {
        projectService.deleteProject(teamId, projectId);
    }

    @PostMapping("/{projectId}/members/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<ProjectResponse> addMemberToProject(@PathVariable Long teamId,
                                                              @PathVariable Long projectId,
                                                              @PathVariable Long userId) {
        ProjectResponse updatedProject = projectService.addMemberToProject(teamId, projectId, userId);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public void removeMemberFromProject(@PathVariable Long teamId,
                                        @PathVariable Long projectId,
                                        @PathVariable Long userId) {
        projectService.removeMemberFromProject(teamId, projectId, userId);
    }
}