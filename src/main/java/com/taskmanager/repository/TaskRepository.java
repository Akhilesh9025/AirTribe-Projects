package com.taskmanager.repository;

import com.taskmanager.entity.Project; // Import Project
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Find tasks by project
    List<Task> findByProject(Project project);

    // Find tasks by assigned user within a project
    List<Task> findByProjectAndAssignedTo(Project project, User assignedTo);

    // Find tasks by created by user within a project
    List<Task> findByProjectAndCreatedBy(Project project, User createdBy);

    // Find tasks by status and project
    List<Task> findByProjectAndStatus(Project project, TaskStatus status);

    // Search tasks by title or description within a project
    @Query("SELECT t FROM Task t WHERE t.project = :project AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Task> searchByProjectAndKeyword(@Param("project") Project project, @Param("keyword") String keyword);

    // Find tasks due before a certain date for a project
    List<Task> findByProjectAndDueDateBefore(Project project, LocalDateTime dueDate);

    // Find a specific task within a project
    Optional<Task> findByIdAndProject(Long taskId, Project project);
}