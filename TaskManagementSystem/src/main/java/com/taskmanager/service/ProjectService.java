package com.taskmanager.service;

import com.taskmanager.dto.request.ProjectCreateRequest;
import com.taskmanager.dto.request.ProjectUpdateRequest;
import com.taskmanager.dto.response.ProjectResponse;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.Team;
import com.taskmanager.entity.User;
import com.taskmanager.entity.enums.UserRole;
import com.taskmanager.exception.BadRequestException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.mapper.ProjectMapper;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.TeamRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final WebSocketService webSocketService;

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));
        }
        throw new BadRequestException("User not authenticated.");
    }

    @Transactional
    public ProjectResponse createProject(Long teamId, ProjectCreateRequest request) {
        User currentUser = getCurrentUser();
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        // Only team creator or admin can create a project within a team
        if (!team.getCreator().equals(currentUser) && !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to create projects in this team.");
        }

        if (projectRepository.existsByNameAndTeam(request.getName(), team)) {
            throw new BadRequestException("Project with name '" + request.getName() + "' already exists in this team.");
        }

        Project project = projectMapper.toEntity(request);
        project.setCreator(currentUser);
        project.setTeam(team);
        project.getMembers().add(currentUser); // Creator is automatically a member of the project

        Project savedProject = projectRepository.save(project);
        webSocketService.notifyTeamUpdate(teamId, "New project '" + savedProject.getName() + "' created in team " + team.getName());
        return projectMapper.toResponse(savedProject);
    }

    public List<ProjectResponse> getProjectsByTeam(Long teamId) {
        User currentUser = getCurrentUser();
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        // Only team members or admin can view projects within a team
        if (!team.getMembers().contains(currentUser) && !team.getCreator().equals(currentUser) && !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to view projects for this team.");
        }

        return projectRepository.findByTeam(team).stream()
                .map(projectMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(Long teamId, Long projectId) {
        User currentUser = getCurrentUser();
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));
        Project project = projectRepository.findByIdAndTeam(projectId, team)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId + " in team " + teamId));

        // Only project members or admin can view the project
        if (!project.getMembers().contains(currentUser) && !project.getCreator().equals(currentUser) && !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to view this project.");
        }
        return projectMapper.toResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long teamId, Long projectId, ProjectUpdateRequest request) {
        User currentUser = getCurrentUser();
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));
        Project project = projectRepository.findByIdAndTeam(projectId, team)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId + " in team " + teamId));

        // Only project creator, team creator, or admin can update
        if (!project.getCreator().equals(currentUser) && !team.getCreator().equals(currentUser) && !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to update this project.");
        }

        if (request.getName() != null && !request.getName().equals(project.getName())) {
            if (projectRepository.existsByNameAndTeam(request.getName(), team)) {
                throw new BadRequestException("Project with name '" + request.getName() + "' already exists in this team.");
            }
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        Project updatedProject = projectRepository.save(project);
        webSocketService.notifyTeamUpdate(teamId, "Project '" + updatedProject.getName() + "' in team " + team.getName() + " was updated.");
        return projectMapper.toResponse(updatedProject);
    }

    @Transactional
    public void deleteProject(Long teamId, Long projectId) {
        User currentUser = getCurrentUser();
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));
        Project project = projectRepository.findByIdAndTeam(projectId, team)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId + " in team " + teamId));

        // Only project creator, team creator, or admin can delete
        if (!project.getCreator().equals(currentUser) && !team.getCreator().equals(currentUser) && !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to delete this project.");
        }

        projectRepository.delete(project);
        webSocketService.notifyTeamUpdate(teamId, "Project '" + project.getName() + "' in team " + team.getName() + " was deleted.");
    }

    @Transactional
    public ProjectResponse addMemberToProject(Long teamId, Long projectId, Long userId) {
        User currentUser = getCurrentUser();
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));
        Project project = projectRepository.findByIdAndTeam(projectId, team)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId + " in team " + teamId));

        // Only project creator, team creator, or admin can add members
        if (!project.getCreator().equals(currentUser) && !team.getCreator().equals(currentUser) && !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to add members to this project.");
        }

        User userToAdd = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Ensure user is part of the parent team
        if (!team.getMembers().contains(userToAdd) && !team.getCreator().equals(userToAdd)) {
            throw new BadRequestException("User must be a member of the parent team before being added to the project.");
        }

        if (project.getMembers().contains(userToAdd)) {
            throw new BadRequestException("User '" + userToAdd.getUsername() + "' is already a member of this project.");
        }

        project.getMembers().add(userToAdd);
        Project updatedProject = projectRepository.save(project);
        webSocketService.notifyTeamUpdate(teamId, userToAdd.getUsername() + " joined project " + project.getName() + " in team " + team.getName());
        webSocketService.notifyUser(userId, "You have been added to project: " + project.getName());
        return projectMapper.toResponse(updatedProject);
    }

    @Transactional
    public ProjectResponse removeMemberFromProject(Long teamId, Long projectId, Long userId) {
        User currentUser = getCurrentUser();
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));
        Project project = projectRepository.findByIdAndTeam(projectId, team)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId + " in team " + teamId));

        // Only project creator, team creator, or admin can remove members
        if (!project.getCreator().equals(currentUser) && !team.getCreator().equals(currentUser) && !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to remove members from this project.");
        }

        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!project.getMembers().contains(userToRemove)) {
            throw new BadRequestException("User '" + userToRemove.getUsername() + "' is not a member of this project.");
        }
        if (project.getCreator().equals(userToRemove)) {
            throw new BadRequestException("Cannot remove the project creator.");
        }

        project.getMembers().remove(userToRemove);
        Project updatedProject = projectRepository.save(project);
        webSocketService.notifyTeamUpdate(teamId, userToRemove.getUsername() + " left project " + project.getName() + " in team " + team.getName());
        webSocketService.notifyUser(userId, "You have been removed from project: " + project.getName());
        return projectMapper.toResponse(updatedProject);
    }
}