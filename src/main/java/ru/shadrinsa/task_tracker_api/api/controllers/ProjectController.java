package ru.shadrinsa.task_tracker_api.api.controllers;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.shadrinsa.task_tracker_api.api.dto.ProjectDto;
import ru.shadrinsa.task_tracker_api.api.exeptions.BadRequestException;
import ru.shadrinsa.task_tracker_api.api.exeptions.NotFoundException;
import ru.shadrinsa.task_tracker_api.api.factories.ProjectDtoFactory;
import ru.shadrinsa.task_tracker_api.store.entities.ProjectEntity;
import ru.shadrinsa.task_tracker_api.store.repositories.ProjectRepository;

import java.util.Objects;

@Transactional
@RestController
@RequiredArgsConstructor
public class ProjectController {

    final private ProjectDtoFactory projectDtoFactory;
    final private ProjectRepository projectRepository;

    public static final String CREATE_PROJECT = "/api/projects";
    public static final String EDIT_PROJECT = "/api/projects/{project_id}";


    @PostMapping(CREATE_PROJECT)
    public ProjectDto createProject(@RequestParam String name){
        if(name.trim().isEmpty()){
            throw new BadRequestException("name is empty.");
        }

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
    @PatchMapping(EDIT_PROJECT)
    public ProjectDto editProject(@PathVariable Long project_id, @RequestParam String name){
        if(name.trim().isEmpty()){
            throw new BadRequestException("name is empty.");
        }

        ProjectEntity project = projectRepository
                .findById(project_id)
                .orElseThrow(() ->
                        new NotFoundException("Project not found")
                );

        projectRepository
                .findByName(name)
                .filter(anotherProject -> !Objects.equals(anotherProject.getId(), project_id))
                .ifPresent(anotherProject -> {
                    throw new BadRequestException("Project already exists.");
                });

        project = projectRepository.saveAndFlush(project);

        return projectDtoFactory.makeProjectDto(project);
    }
}
