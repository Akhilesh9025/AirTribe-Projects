package com.taskmanager.controller;

import com.taskmanager.dto.request.TeamCreateRequest;
import com.taskmanager.dto.request.TeamUpdateRequest;
import com.taskmanager.dto.response.TeamResponse;
import com.taskmanager.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody TeamCreateRequest request) {
        TeamResponse team = teamService.createTeam(request);
        return new ResponseEntity<>(team, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<TeamResponse>> getAllTeams() {
        List<TeamResponse> teams = teamService.getAllTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{teamId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<TeamResponse> getTeamById(@PathVariable Long teamId) {
        TeamResponse team = teamService.getTeamById(teamId);
        return ResponseEntity.ok(team);
    }

    @PutMapping("/{teamId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<TeamResponse> updateTeam(@PathVariable Long teamId,
                                                   @Valid @RequestBody TeamUpdateRequest request) {
        TeamResponse updatedTeam = teamService.updateTeam(teamId, request);
        return ResponseEntity.ok(updatedTeam);
    }

    @DeleteMapping("/{teamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public void deleteTeam(@PathVariable Long teamId) {
        teamService.deleteTeam(teamId);
    }

    @PostMapping("/{teamId}/members/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<TeamResponse> addMemberToTeam(@PathVariable Long teamId, @PathVariable Long userId) {
        TeamResponse updatedTeam = teamService.addMemberToTeam(teamId, userId);
        return ResponseEntity.ok(updatedTeam);
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public void removeMemberFromTeam(@PathVariable Long teamId, @PathVariable Long userId) {
        teamService.removeMemberFromTeam(teamId, userId);
    }
}