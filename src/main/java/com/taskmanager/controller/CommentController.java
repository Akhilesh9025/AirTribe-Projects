package com.taskmanager.controller;

import com.taskmanager.dto.request.CommentCreateRequest;
import com.taskmanager.dto.request.CommentUpdateRequest;
import com.taskmanager.dto.response.CommentResponse;
import com.taskmanager.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams/{teamId}/projects/{projectId}/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long teamId,
                                                      @PathVariable Long projectId,
                                                      @PathVariable Long taskId,
                                                      @Valid @RequestBody CommentCreateRequest request) {
        CommentResponse comment = commentService.addComment(teamId, projectId, taskId, request);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<CommentResponse>> getCommentsByTask(@PathVariable Long teamId,
                                                                   @PathVariable Long projectId,
                                                                   @PathVariable Long taskId) {
        List<CommentResponse> comments = commentService.getCommentsByTask(teamId, projectId, taskId);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/{commentId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<CommentResponse> updateComment(@PathVariable Long teamId,
                                                         @PathVariable Long projectId,
                                                         @PathVariable Long taskId,
                                                         @PathVariable Long commentId,
                                                         @Valid @RequestBody CommentUpdateRequest request) {
        CommentResponse updatedComment = commentService.updateComment(teamId, projectId, taskId, commentId, request);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public void deleteComment(@PathVariable Long teamId,
                              @PathVariable Long projectId,
                              @PathVariable Long taskId,
                              @PathVariable Long commentId) {
        commentService.deleteComment(teamId, projectId, taskId, commentId);
    }
}