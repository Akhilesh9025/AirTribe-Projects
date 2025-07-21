package com.taskmanager.repository;

import com.taskmanager.entity.Project;
import com.taskmanager.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByTeam(Team team);
    Optional<Project> findByIdAndTeam(Long projectId, Team team);
    boolean existsByNameAndTeam(String name, Team team); // Project name unique within a team
}