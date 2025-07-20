package com.taskmanager.controller;

import com.taskmanager.dto.response.AttachmentResponse;
import com.taskmanager.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/teams/{teamId}/projects/{projectId}/tasks/{taskId}/attachments") // New nested path
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<AttachmentResponse> uploadAttachment(@PathVariable Long teamId,
                                                               @PathVariable Long projectId,
                                                               @PathVariable Long taskId,
                                                               @RequestParam("file") MultipartFile file) {
        AttachmentResponse attachment = attachmentService.uploadAttachment(teamId, projectId, taskId, file);
        return new ResponseEntity<>(attachment, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<AttachmentResponse>> getAttachmentsByTask(@PathVariable Long teamId,
                                                                         @PathVariable Long projectId,
                                                                         @PathVariable Long taskId) {
        List<AttachmentResponse> attachments = attachmentService.getAttachmentsByTask(teamId, projectId, taskId);
        return ResponseEntity.ok(attachments);
    }

    @GetMapping("/{filename:.+}") // Regex to allow file extension
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long teamId,
                                                       @PathVariable Long projectId,
                                                       @PathVariable Long taskId,
                                                       @PathVariable String filename,
                                                       HttpServletRequest request) {
        Resource resource = attachmentService.downloadAttachment(teamId, projectId, taskId, filename);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // Fallback to default content type if MIME type could not be determined
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public void deleteAttachment(@PathVariable Long teamId,
                                 @PathVariable Long projectId,
                                 @PathVariable Long taskId,
                                 @PathVariable Long attachmentId) {
        attachmentService.deleteAttachment(teamId, projectId, taskId, attachmentId);
    }
}