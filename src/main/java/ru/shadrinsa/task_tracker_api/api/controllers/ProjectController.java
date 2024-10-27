package ru.shadrinsa.task_tracker_api.api.controllers;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.shadrinsa.task_tracker_api.api.dto.AckDto;
import ru.shadrinsa.task_tracker_api.api.dto.ProjectDto;
import ru.shadrinsa.task_tracker_api.api.exeptions.BadRequestException;
import ru.shadrinsa.task_tracker_api.api.exeptions.NotFoundException;
import ru.shadrinsa.task_tracker_api.api.factories.ProjectDtoFactory;
import ru.shadrinsa.task_tracker_api.store.entities.ProjectEntity;
import ru.shadrinsa.task_tracker_api.store.repositories.ProjectRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Transactional
@RestController
@RequiredArgsConstructor
public class ProjectController {

    final private ProjectDtoFactory projectDtoFactory;
    final private ProjectRepository projectRepository;

    public static final String FETCH_PROJECT = "/api/projects";
    public static final String CREATE_PROJECT = "/api/projects";
    public static final String EDIT_PROJECT = "/api/projects/{project_id}";
    public static final String DELETE_PROJECT = "/api/projects/{project_id}";


    @GetMapping(FETCH_PROJECT)
    public List<ProjectDto> fetchProjects(@RequestParam(value = "prefix_name", required = false)Optional<String> optionalPrefixName){
        optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.trim().isEmpty());
        Stream<ProjectEntity> projectStream = optionalPrefixName
                .map(projectRepository::streamAllByNameStartsWithIgnoreCase)
                .orElseGet(projectRepository::streamAll);
        return projectStream
                .map(projectDtoFactory::makeProjectDto)
                .toList();
    }

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

        ProjectEntity project = getProjectOrThrowException(project_id);

        projectRepository
                .findByName(name)
                .filter(anotherProject -> !Objects.equals(anotherProject.getId(), project_id))
                .ifPresent(anotherProject -> {
                    throw new BadRequestException("Project already exists.");
                });

        project = projectRepository.saveAndFlush(project);

        return projectDtoFactory.makeProjectDto(project);
    }
    @DeleteMapping(DELETE_PROJECT)
    public AckDto deleteProject(@PathVariable Long project_id){
        ProjectEntity project = getProjectOrThrowException(project_id);
        projectRepository.deleteById(project_id);
        return AckDto.makeDefault(true);
    }

    private ProjectEntity getProjectOrThrowException(Long project_id) {
        return projectRepository
                .findById(project_id)
                .orElseThrow(() ->
                        new NotFoundException("Project not found")
                );
    }
}
