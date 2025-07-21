package com.taskmanager.service;

import com.taskmanager.dto.response.AttachmentResponse;
import com.taskmanager.entity.Attachment;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.entity.enums.UserRole;
import com.taskmanager.exception.BadRequestException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.mapper.AttachmentMapper; // Assuming MapStruct
import com.taskmanager.repository.AttachmentRepository;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentService {

    @Value("${file.upload-dir}")
    private String uploadDir;
    private Path fileStorageLocation;

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AttachmentMapper attachmentMapper;
    private final WebSocketService webSocketService;

    // Initialize file storage directory
    @jakarta.annotation.PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir)
                .toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("File upload directory created at: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            log.error("Could not create the directory where the uploaded files will be stored.", ex);
            throw new BadRequestException("Could not create upload directory. " + ex.getMessage());
        }
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));
        }
        throw new BadRequestException("User not authenticated.");
    }

    @Transactional
    public AttachmentResponse uploadAttachment(Long teamId, Long projectId, Long taskId, MultipartFile file) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        if (!project.getTeam().getId().equals(teamId)) {
            throw new BadRequestException("Project with ID " + projectId + " does not belong to team with ID " + teamId);
        }

        Task task = taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId + " in project " + projectId));

        // Only project members, or team creator, or admin can upload attachments
        if (!project.getMembers().contains(currentUser) &&
                !project.getCreator().equals(currentUser) &&
                !project.getTeam().getCreator().equals(currentUser) &&
                !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to upload attachments to tasks in this project.");
        }

        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFileName.substring(dotIndex);
        }
        // Generate a unique file name to prevent overwrites
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            log.error("Could not store file {} Reason: {}", originalFileName, ex.getMessage());
            throw new BadRequestException("Could not store file " + originalFileName + ". Please try again!");
        }

        Attachment attachment = new Attachment();
        attachment.setFilename(originalFileName);
        attachment.setFileUrl("/api/tasks/" + taskId + "/attachments/" + uniqueFileName); // URL for download
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setTask(task);
        attachment.setUploadedBy(currentUser);

        Attachment savedAttachment = attachmentRepository.save(attachment);
        webSocketService.notifyTaskUpdate(taskId, "New attachment uploaded to task '" + task.getTitle() + "' by " + currentUser.getUsername());
        return attachmentMapper.toResponse(savedAttachment);
    }

    public Resource downloadAttachment(Long teamId, Long projectId, Long taskId, String filename) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        if (!project.getTeam().getId().equals(teamId)) {
            throw new BadRequestException("Project with ID " + projectId + " does not belong to team with ID " + teamId);
        }

        Task task = taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId + " in project " + projectId));

        // Only project members, or team creator, or admin can download attachments
        if (!project.getMembers().contains(currentUser) &&
                !project.getCreator().equals(currentUser) &&
                !project.getTeam().getCreator().equals(currentUser) &&
                !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to download attachments from tasks in this project.");
        }

        // Find the attachment based on task and its URL/filename logic
        // For security and to prevent path traversal, we fetch by URL stored in DB
        Attachment attachment = attachmentRepository.findByTask(task).stream()
                .filter(att -> att.getFileUrl().contains(filename)) // Simplified check, better to store unique_name
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "filename", filename + " for task " + taskId));

        Path filePath = this.fileStorageLocation.resolve(filename).normalize(); // filename here refers to the actual unique file name on disk
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found", "path", filePath.toString());
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File not found", "path", filePath.toString());
        }
    }


    public List<AttachmentResponse> getAttachmentsByTask(Long teamId, Long projectId, Long taskId) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        if (!project.getTeam().getId().equals(teamId)) {
            throw new BadRequestException("Project with ID " + projectId + " does not belong to team with ID " + teamId);
        }

        Task task = taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId + " in project " + projectId));

        // Only project members, or team creator, or admin can view attachments
        if (!project.getMembers().contains(currentUser) &&
                !project.getCreator().equals(currentUser) &&
                !project.getTeam().getCreator().equals(currentUser) &&
                !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to view attachments for this task.");
        }

        return attachmentRepository.findByTask(task).stream()
                .map(attachmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAttachment(Long teamId, Long projectId, Long taskId, Long attachmentId) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        if (!project.getTeam().getId().equals(teamId)) {
            throw new BadRequestException("Project with ID " + projectId + " does not belong to team with ID " + teamId);
        }

        Task task = taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId + " in project " + projectId));

        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", attachmentId));

        if (!attachment.getTask().equals(task)) {
            throw new BadRequestException("Attachment with ID " + attachmentId + " does not belong to task " + taskId);
        }

        // Only uploader, task creator, project creator, team creator, or admin can delete
        if (!attachment.getUploadedBy().equals(currentUser) &&
                !task.getCreatedBy().equals(currentUser) &&
                !project.getCreator().equals(currentUser) &&
                !project.getTeam().getCreator().equals(currentUser) &&
                !currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
            throw new BadRequestException("You are not authorized to delete this attachment.");
        }

        // Delete file from storage
        Path filePath = Paths.get(uploadDir, attachment.getFileUrl().substring(attachment.getFileUrl().lastIndexOf('/') + 1)).normalize();
        try {
            Files.deleteIfExists(filePath);
            log.info("Deleted file: {}", filePath);
        } catch (IOException ex) {
            log.error("Could not delete file {}: {}", filePath, ex.getMessage());
        }

        attachmentRepository.delete(attachment);
        webSocketService.notifyTaskUpdate(taskId, "Attachment '" + attachment.getFilename() + "' deleted from task '" + task.getTitle() + "' by " + currentUser.getUsername());
    }
}