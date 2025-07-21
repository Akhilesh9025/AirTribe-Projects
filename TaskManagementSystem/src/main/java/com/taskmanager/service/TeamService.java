package com.taskmanager.service;

import com.taskmanager.dto.request.TeamCreateRequest;
import com.taskmanager.dto.request.TeamUpdateRequest;
import com.taskmanager.dto.response.TeamResponse;
import com.taskmanager.entity.Team;
import com.taskmanager.entity.User;
import com.taskmanager.entity.enums.UserRole;
import com.taskmanager.exception.BadRequestException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.mapper.TeamMapper; // Assuming MapStruct
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
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamMapper teamMapper;
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
    public TeamResponse createTeam(TeamCreateRequest request) {
        User currentUser = getCurrentUser();
        if (teamRepository.existsByName(request.getName())) {
            throw new BadRequestException("Team with name '" + request.getName() + "' already exists.");
        }

        Team team = teamMapper.toEntity(request);
        team.setCreator(currentUser);
        team.getMembers().add(currentUser); // Creator is automatically a member

        Team savedTeam = teamRepository.save(team);
        return teamMapper.toResponse(savedTeam);
    }

    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(teamMapper::toResponse)
                .collect(Collectors.toList());
    }

    public TeamResponse getTeamById(Long teamId) {
        User currentUser = getCurrentUser();
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        if (!team.getMembers().contains(currentUser) && !team.getCreator().equals(currentUser) && !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to view this team.");
        }
        return teamMapper.toResponse(team);
    }

    @Transactional
    public TeamResponse updateTeam(Long teamId, TeamUpdateRequest request) {
        User currentUser = getCurrentUser();
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        if (!team.getCreator().equals(currentUser) && !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to update this team.");
        }

        if (request.getName() != null && !request.getName().equals(team.getName())) {
            if (teamRepository.existsByName(request.getName())) {
                throw new BadRequestException("Team with name '" + request.getName() + "' already exists.");
            }
            team.setName(request.getName());
        }
        if (request.getDescription() != null) {
            team.setDescription(request.getDescription());
        }

        Team updatedTeam = teamRepository.save(team);
        webSocketService.notifyTeamUpdate(teamId, "Team '" + updatedTeam.getName() + "' was updated.");
        return teamMapper.toResponse(updatedTeam);
    }

    @Transactional
    public void deleteTeam(Long teamId) {
        User currentUser = getCurrentUser();
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        if (!team.getCreator().equals(currentUser) && !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to delete this team.");
        }

        teamRepository.delete(team);
        webSocketService.notifyTeamUpdate(teamId, "Team '" + team.getName() + "' was deleted.");
    }

    @Transactional
    public TeamResponse addMemberToTeam(Long teamId, Long userId) {
        User currentUser = getCurrentUser();
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        if (!team.getCreator().equals(currentUser) && !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to add members to this team.");
        }

        User userToAdd = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (team.getMembers().contains(userToAdd)) {
            throw new BadRequestException("User '" + userToAdd.getUsername() + "' is already a member of this team.");
        }

        team.getMembers().add(userToAdd);
        Team updatedTeam = teamRepository.save(team);
        webSocketService.notifyTeamUpdate(teamId, userToAdd.getUsername() + " joined team " + team.getName());
        webSocketService.notifyUser(userId, "You have been added to team: " + team.getName());
        return teamMapper.toResponse(updatedTeam);
    }

    @Transactional
    public TeamResponse removeMemberFromTeam(Long teamId, Long userId) {
        User currentUser = getCurrentUser();
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));

        if (!team.getCreator().equals(currentUser) && !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to remove members from this team.");
        }

        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!team.getMembers().contains(userToRemove)) {
            throw new BadRequestException("User '" + userToRemove.getUsername() + "' is not a member of this team.");
        }
        if (team.getCreator().equals(userToRemove)) {
            throw new BadRequestException("Cannot remove the team creator.");
        }

        team.getMembers().remove(userToRemove);
        Team updatedTeam = teamRepository.save(team);
        webSocketService.notifyTeamUpdate(teamId, userToRemove.getUsername() + " left team " + team.getName());
        webSocketService.notifyUser(userId, "You have been removed from team: " + team.getName());
        return teamMapper.toResponse(updatedTeam);
    }
}