package com.taskmanager.service;

import com.taskmanager.dto.request.CommentCreateRequest;
import com.taskmanager.dto.request.CommentUpdateRequest;
import com.taskmanager.dto.response.CommentResponse;
import com.taskmanager.entity.Comment;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.entity.enums.UserRole;
import com.taskmanager.exception.BadRequestException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.mapper.CommentMapper; // Assuming MapStruct
import com.taskmanager.repository.CommentRepository;
import com.taskmanager.repository.ProjectRepository; // New dependency
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository; // New dependency
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
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
    public CommentResponse addComment(Long teamId, Long projectId, Long taskId, CommentCreateRequest request) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        if (!project.getTeam().getId().equals(teamId)) {
            throw new BadRequestException("Project with ID " + projectId + " does not belong to team with ID " + teamId);
        }

        Task task = taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId + " in project " + projectId));

        // Only project members, or team creator, or admin can comment on tasks in this project
        if (!project.getMembers().contains(currentUser) &&
                !project.getCreator().equals(currentUser) &&
                !project.getTeam().getCreator().equals(currentUser) &&
                !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to add comments to tasks in this project.");
        }

        Comment comment = commentMapper.toEntity(request);
        comment.setTask(task);
        comment.setAuthor(currentUser);

        Comment savedComment = commentRepository.save(comment);

        // Notify task creator and assigned user
        if (!task.getCreatedBy().equals(currentUser)) {
            webSocketService.notifyUser(task.getCreatedBy().getId(),
                    "New comment on your task '" + task.getTitle() + "' from " + currentUser.getUsername()
            );
        }
        if (task.getAssignedTo() != null && !task.getAssignedTo().equals(currentUser) && !task.getAssignedTo().equals(task.getCreatedBy())) {
            webSocketService.notifyUser(task.getAssignedTo().getId(),
                    "New comment on your assigned task '" + task.getTitle() + "' from " + currentUser.getUsername()
            );
        }
        // Notify other project members about new comment on task
        project.getMembers().forEach(member -> {
            if (!member.equals(currentUser) &&
                    !member.equals(task.getCreatedBy()) &&
                    (task.getAssignedTo() == null || !member.equals(task.getAssignedTo()))) {
                webSocketService.notifyUser(
                        member.getId(),
                        "New comment on task '" + task.getTitle() + "' in project " + project.getName()
                );
            }
        });

        webSocketService.notifyTaskUpdate(taskId, "New comment on task: " + task.getTitle()); // Generic update

        return commentMapper.toResponse(savedComment);
    }

    public List<CommentResponse> getCommentsByTask(Long teamId, Long projectId, Long taskId) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        if (!project.getTeam().getId().equals(teamId)) {
            throw new BadRequestException("Project with ID " + projectId + " does not belong to team with ID " + teamId);
        }

        Task task = taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId + " in project " + projectId));

        // Only project members, or team creator, or admin can view comments
        if (!project.getMembers().contains(currentUser) &&
                !project.getCreator().equals(currentUser) &&
                !project.getTeam().getCreator().equals(currentUser) &&
                !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to view comments for this task.");
        }

        return commentRepository.findByTask(task).stream()
                .map(commentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse updateComment(Long teamId, Long projectId, Long taskId, Long commentId, CommentUpdateRequest request) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        if (!project.getTeam().getId().equals(teamId)) {
            throw new BadRequestException("Project with ID " + projectId + " does not belong to team with ID " + teamId);
        }

        Task task = taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId + " in project " + projectId));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        if (!comment.getTask().equals(task)) { // Ensure comment belongs to the correct task
            throw new BadRequestException("Comment with ID " + commentId + " does not belong to task " + taskId);
        }

        // Only comment author or admin can update
        if (!comment.getAuthor().equals(currentUser) && !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to update this comment.");
        }

        if (request.getContent() != null) {
            comment.setContent(request.getContent());
        }

        Comment updatedComment = commentRepository.save(comment);
        webSocketService.notifyTaskUpdate(taskId, "Comment on task '" + task.getTitle() + "' updated by " + currentUser.getUsername());
        return commentMapper.toResponse(updatedComment);
    }

    @Transactional
    public void deleteComment(Long teamId, Long projectId, Long taskId, Long commentId) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        if (!project.getTeam().getId().equals(teamId)) {
            throw new BadRequestException("Project with ID " + projectId + " does not belong to team with ID " + teamId);
        }

        Task task = taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId + " in project " + projectId));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        if (!comment.getTask().equals(task)) {
            throw new BadRequestException("Comment with ID " + commentId + " does not belong to task " + taskId);
        }

        // Only comment author, task creator, project creator, team creator, or admin can delete
        if (!comment.getAuthor().equals(currentUser) &&
                !task.getCreatedBy().equals(currentUser) &&
                !project.getCreator().equals(currentUser) &&
                !project.getTeam().getCreator().equals(currentUser) &&
                !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to delete this comment.");
        }

        commentRepository.delete(comment);
        webSocketService.notifyTaskUpdate(taskId, "Comment on task '" + task.getTitle() + "' deleted by " + currentUser.getUsername());
    }
}