package com.taskmanager.service;

import com.taskmanager.dto.request.TaskCreateRequest;
import com.taskmanager.dto.request.TaskUpdateRequest;
import com.taskmanager.dto.response.TaskResponse;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.entity.enums.TaskStatus;
import com.taskmanager.entity.enums.UserRole;
import com.taskmanager.exception.BadRequestException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.mapper.TaskMapper;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskMapper taskMapper;
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
    public TaskResponse createTask(Long teamId, Long projectId, TaskCreateRequest request) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        // Verify project belongs to the specified team
        if (!project.getTeam().getId().equals(teamId)) {
            throw new BadRequestException("Project with ID " + projectId + " does not belong to team with ID " + teamId);
        }

        // Check if current user is a member of the project or its parent team creator
        if (!project.getMembers().contains(currentUser) &&
                !project.getCreator().equals(currentUser) &&
                !project.getTeam().getCreator().equals(currentUser) &&
                !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to create tasks in this project.");
        }

        Task task = taskMapper.toEntity(request);
        task.setCreatedBy(currentUser);
        task.setProject(project);

        if (request.getAssignedToUserId() != null) {
            User assignedTo = userRepository.findById(request.getAssignedToUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssignedToUserId()));
            // Ensure assigned user is also a member of the project or its parent team
            if (!project.getMembers().contains(assignedTo) &&
                    !project.getCreator().equals(assignedTo) &&
                    !project.getTeam().getMembers().contains(assignedTo) &&
                    !project.getTeam().getCreator().equals(assignedTo)) {
                throw new BadRequestException("Assigned user is not a member of this project or its parent team.");
            }
            task.setAssignedTo(assignedTo);
        }

        Task savedTask = taskRepository.save(task);

        if (savedTask.getAssignedTo() != null) {
            webSocketService.notifyUser(
                    savedTask.getAssignedTo().getId(),
                    "Task assigned: " + savedTask.getTitle() + " in project " + project.getName()
            );
        }
        // Notify project members (excluding creator/assigned)
        project.getMembers().forEach(member -> {
            if (!member.equals(currentUser) && (savedTask.getAssignedTo() == null || !member.equals(savedTask.getAssignedTo()))) {
                webSocketService.notifyUser(
                        member.getId(),
                        "New task created: " + savedTask.getTitle() + " in project " + project.getName()
                );
            }
        });

        project.getTeam().getMembers().forEach(member -> {
            if (!project.getMembers().contains(member) && !member.equals(currentUser) && (savedTask.getAssignedTo() == null || !member.equals(savedTask.getAssignedTo()))) {
                webSocketService.notifyUser(
                        member.getId(),
                        "New task created in project " + project.getName() + ": " + savedTask.getTitle() + " in team " + project.getTeam().getName()
                );
            }
        });


        return taskMapper.toResponse(savedTask);
    }

    public List<TaskResponse> getTasks(Long teamId, Long projectId, String status, String assignedTo, String search, String sortBy, String sortDir) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        if (!project.getTeam().getId().equals(teamId)) {
            throw new BadRequestException("Project with ID " + projectId + " does not belong to team with ID " + teamId);
        }

        if (!project.getMembers().contains(currentUser) && !project.getCreator().equals(currentUser) && !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to view tasks for this project.");
        }

        List<Task> tasks;
        if (search != null && !search.isEmpty()) {
            tasks = taskRepository.searchByProjectAndKeyword(project, search);
        } else {
            tasks = taskRepository.findByProject(project);
        }

        if (status != null && !status.isEmpty()) {
            TaskStatus taskStatus = TaskStatus.valueOf(status.toUpperCase());
            tasks = tasks.stream()
                    .filter(t -> t.getStatus().equals(taskStatus))
                    .collect(Collectors.toList());
        }

        if (assignedTo != null && assignedTo.equalsIgnoreCase("me")) {
            tasks = tasks.stream()
                    .filter(t -> t.getAssignedTo() != null && t.getAssignedTo().equals(currentUser))
                    .collect(Collectors.toList());
        } else if (assignedTo != null && !assignedTo.isEmpty()) {
            try {
                Long assignedUserId = Long.parseLong(assignedTo);
                User assignedUser = userRepository.findById(assignedUserId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", assignedUserId));
                tasks = tasks.stream()
                        .filter(t -> t.getAssignedTo() != null && t.getAssignedTo().equals(assignedUser))
                        .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                throw new BadRequestException("Invalid 'assignedTo' parameter. Use 'me' or a valid user ID.");
            }
        }

        if (sortBy != null && !sortBy.isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
            switch (sortBy.toLowerCase()) {
                case "duedate":
                    tasks.sort((t1, t2) -> {
                        if (t1.getDueDate() == null && t2.getDueDate() == null) return 0;
                        if (t1.getDueDate() == null) return direction == Sort.Direction.ASC ? 1 : -1;
                        if (t2.getDueDate() == null) return direction == Sort.Direction.ASC ? -1 : 1;
                        return direction == Sort.Direction.ASC ? t1.getDueDate().compareTo(t2.getDueDate()) : t2.getDueDate().compareTo(t1.getDueDate());
                    });
                    break;
                case "priority":
                    tasks.sort((t1, t2) -> direction == Sort.Direction.ASC ? t1.getPriority().compareTo(t2.getPriority()) : t2.getPriority().compareTo(t1.getPriority()));
                    break;
                case "createdat":
                    tasks.sort((t1, t2) -> direction == Sort.Direction.ASC ? t1.getCreatedAt().compareTo(t2.getCreatedAt()) : t2.getCreatedAt().compareTo(t1.getCreatedAt()));
                    break;
                default:
                    tasks.sort((t1, t2) -> direction == Sort.Direction.ASC ? t1.getTitle().compareTo(t2.getTitle()) : t2.getTitle().compareTo(t1.getTitle()));
                    break;
            }
        }

        return tasks.stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse updateTask(Long teamId, Long projectId, Long taskId, TaskUpdateRequest request) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        if (!project.getTeam().getId().equals(teamId)) {
            throw new BadRequestException("Project with ID " + projectId + " does not belong to team with ID " + teamId);
        }

        Task task = taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId + " in project " + projectId));

        // Only task creator, assigned user, project creator, team creator, or admin can update
        if (!task.getCreatedBy().equals(currentUser) &&
                (task.getAssignedTo() == null || !task.getAssignedTo().equals(currentUser)) &&
                !project.getCreator().equals(currentUser) &&
                !project.getTeam().getCreator().equals(currentUser) &&
                !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to update this task.");
        }

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
            if (request.getStatus() == TaskStatus.COMPLETED) {
                webSocketService.notifyUser(
                        task.getCreatedBy().getId(),
                        "Task '" + task.getTitle() + "' was completed by " + currentUser.getUsername()
                );
                if (task.getAssignedTo() != null && !task.getAssignedTo().equals(currentUser)) {
                    webSocketService.notifyUser(
                            task.getAssignedTo().getId(),
                            "Your assigned task '" + task.getTitle() + "' was completed by " + currentUser.getUsername()
                    );
                }
            }
        }

        if (request.getAssignedToUserId() != null) {
            User newAssignedTo = userRepository.findById(request.getAssignedToUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssignedToUserId()));
            // Ensure new assigned user is a member of the project or its parent team
            if (!project.getMembers().contains(newAssignedTo) &&
                    !project.getCreator().equals(newAssignedTo) &&
                    !project.getTeam().getMembers().contains(newAssignedTo) &&
                    !project.getTeam().getCreator().equals(newAssignedTo)) {
                throw new BadRequestException("New assigned user is not a member of this project or its parent team.");
            }
            task.setAssignedTo(newAssignedTo);
            webSocketService.notifyUser(
                    newAssignedTo.getId(),
                    "You have been assigned to task: " + task.getTitle() + " in project " + project.getName()
            );
        } else if (request.getAssignedToUserId() == null && task.getAssignedTo() != null) {
            User previouslyAssigned = task.getAssignedTo();
            task.setAssignedTo(null);
            webSocketService.notifyUser(
                    previouslyAssigned.getId(),
                    "You have been unassigned from task: " + task.getTitle() + " in project " + project.getName()
            );
        }

        Task updatedTask = taskRepository.save(task);
        webSocketService.notifyTaskUpdate(updatedTask.getId(), updatedTask.getTitle() + " was updated by " + currentUser.getUsername());

        return taskMapper.toResponse(updatedTask);
    }

    @Transactional
    public void deleteTask(Long teamId, Long projectId, Long taskId) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        if (!project.getTeam().getId().equals(teamId)) {
            throw new BadRequestException("Project with ID " + projectId + " does not belong to team with ID " + teamId);
        }

        Task task = taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId + " in project " + projectId));

        // Only task creator, project creator, team creator, or admin can delete
        if (!task.getCreatedBy().equals(currentUser) &&
                !project.getCreator().equals(currentUser) &&
                !project.getTeam().getCreator().equals(currentUser) &&
                !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to delete this task.");
        }

        taskRepository.delete(task);
        webSocketService.notifyProjectUpdate(projectId, "Task '" + task.getTitle() + "' was deleted from project " + project.getName() + " by " + currentUser.getUsername());
    }

    public TaskResponse getTaskById(Long teamId, Long projectId, Long taskId) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        if (!project.getTeam().getId().equals(teamId)) {
            throw new BadRequestException("Project with ID " + projectId + " does not belong to team with ID " + teamId);
        }

        if (!project.getMembers().contains(currentUser) && !project.getCreator().equals(currentUser) && !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to view this task.");
        }

        Task task = taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId + " in project " + projectId));
        return taskMapper.toResponse(task);
    }
}