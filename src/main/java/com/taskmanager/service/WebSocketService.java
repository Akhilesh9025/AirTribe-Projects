package com.taskmanager.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Sends a notification to a specific user.
     * Clients should subscribe to `/topic/users/{userId}/notifications`.
     * @param userId The ID of the user to notify.
     * @param message The notification message.
     */
    public void notifyUser(Long userId, String message) {
        String destination = "/topic/users/" + userId + "/notifications";
        log.info("Sending notification to user {}: {}", userId, message);
        messagingTemplate.convertAndSend(destination, message);
    }

    /**
     * Sends an update about a specific task.
     * Clients can subscribe to `/topic/tasks/{taskId}` for granular updates.
     * @param taskId The ID of the task that was updated.
     * @param message The update message.
     */
    public void notifyTaskUpdate(Long taskId, String message) {
        String destination = "/topic/tasks/" + taskId;
        log.info("Sending task update for task {}: {}", taskId, message);
        messagingTemplate.convertAndSend(destination, message);
    }

    /**
     * Sends an update to all members of a specific team.
     * Clients can subscribe to `/topic/teams/{teamId}/updates`.
     * @param teamId The ID of the team.
     * @param message The update message for the team.
     */
    public void notifyTeamUpdate(Long teamId, String message) {
        String destination = "/topic/teams/" + teamId + "/updates";
        log.info("Sending team update for team {}: {}", teamId, message);
        messagingTemplate.convertAndSend(destination, message);
    }

    /**
     * Sends an update to all members of a specific project.
     * Clients can subscribe to `/topic/projects/{projectId}/updates`.
     * @param projectId The ID of the project.
     * @param message The update message for the project.
     */
    public void notifyProjectUpdate(Long projectId, String message) {
        String destination = "/topic/projects/" + projectId + "/updates";
        log.info("Sending project update for project {}: {}", projectId, message);
        messagingTemplate.convertAndSend(destination, message);
    }
}