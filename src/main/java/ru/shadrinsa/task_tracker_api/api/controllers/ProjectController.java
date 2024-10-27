package ru.shadrinsa.task_tracker_api.api.controllers;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.shadrinsa.task_tracker_api.api.dto.ProjectDto;
import ru.shadrinsa.task_tracker_api.api.exeptions.BadRequestException;
import ru.shadrinsa.task_tracker_api.api.factories.ProjectDtoFactory;
import ru.shadrinsa.task_tracker_api.store.entities.ProjectEntity;
import ru.shadrinsa.task_tracker_api.store.repositories.ProjectRepository;

@Transactional
@RestController
@RequiredArgsConstructor
public class ProjectController {

    final private ProjectDtoFactory projectDtoFactory;
    final private ProjectRepository projectRepository;

    public static final String CREATE_PROJECT = "/api/projects";

    @PostMapping(CREATE_PROJECT)
    public ProjectDto createProject(@RequestParam String name){
        projectRepository
                .findByName(name)
                .ifPresent(project -> {
                     throw new BadRequestException("Project already exists.");
                });
        ProjectEntity project = projectRepository.saveAndFlush(
                ProjectEntity.builder()
                        .name(name)
                        .build()
        );
        return projectDtoFactory.makeProjectDto(project);
    }
}
