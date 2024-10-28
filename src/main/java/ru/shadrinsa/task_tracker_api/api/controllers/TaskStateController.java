package ru.shadrinsa.task_tracker_api.api.controllers;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.shadrinsa.task_tracker_api.api.controllers.helpers.ControllerHelper;
import ru.shadrinsa.task_tracker_api.api.dto.TaskStateDto;
import ru.shadrinsa.task_tracker_api.api.exeptions.BadRequestException;
import ru.shadrinsa.task_tracker_api.api.exeptions.NotFoundException;
import ru.shadrinsa.task_tracker_api.api.factories.TaskStateDtoFactory;
import ru.shadrinsa.task_tracker_api.store.entities.ProjectEntity;
import ru.shadrinsa.task_tracker_api.store.entities.TaskStateEntity;
import ru.shadrinsa.task_tracker_api.store.repositories.TaskStateRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Transactional
@RestController
@RequiredArgsConstructor
public class TaskStateController {

    private final TaskStateRepository taskStateRepository;
    private final TaskStateDtoFactory taskStateDtoFactory;
    private final ControllerHelper controllerHelper;

    public static final String GET_TASK_STATES = "/api/projects/{project_id}/task-states";
    public static final String CREATE_TASK_STATES = "/api/projects/{project_id}/task-state";
    public static final String UPDATE_TASK_STATES = "/api/projects/task-state/{task_state_id}";


    @GetMapping(GET_TASK_STATES)
    public List<TaskStateDto> getTaskStates(@PathVariable(name = "project_id") Long projectId) {

        ProjectEntity project = controllerHelper.getProjectOrThrowException(projectId);

        return project
                .getTaskStates()
                .stream()
                .map(taskStateDtoFactory::makeTaskStateDto)
                .toList();
    }

    @PostMapping(CREATE_TASK_STATES)
    public TaskStateDto createTaskState(@PathVariable(name = "project_id") Long projectId,
                                        @RequestParam(name = "task_state_name") String taskStateName) {
        if (taskStateName.trim().isEmpty()) {
            throw new BadRequestException("Task state name is empty");
        }
        ProjectEntity project = controllerHelper.getProjectOrThrowException(projectId);

        Optional<TaskStateEntity> optionalAnotherTaskState = Optional.empty();

        for (TaskStateEntity taskState : project.getTaskStates()) {
            if (taskState.getName().equalsIgnoreCase(taskStateName)) {
                throw new BadRequestException("Task state already exists.");
            }
            if (!taskState.getRightTaskState().isPresent()) {
                optionalAnotherTaskState = Optional.of(taskState);
                break;
            }
        }


        final TaskStateEntity taskState = taskStateRepository.saveAndFlush(
                TaskStateEntity.builder()
                        .name(taskStateName)
                        .project(project)
                        .build()
        );

        optionalAnotherTaskState
                .ifPresent(anotherTaskState -> {
                    taskState.setLeftTaskState(anotherTaskState);

                    anotherTaskState.setRightTaskState(taskState);

                    taskStateRepository.saveAndFlush(taskState);
                });

        final TaskStateEntity savedTaskState = taskStateRepository.saveAndFlush(taskState);

        return taskStateDtoFactory.makeTaskStateDto(savedTaskState);
    }
    @PatchMapping(UPDATE_TASK_STATES)
    public TaskStateDto updateTaskState(@PathVariable(name = "task_state_id") Long taskStateId,
                                        @RequestParam(name = "task_state_name") String taskStateName){
        if (taskStateName.trim().isEmpty()) {
            throw new BadRequestException("Task state name is empty");
        }
        TaskStateEntity taskState = getTaskStateOrThrowException(taskStateId);
        taskStateRepository
                .findTaskStateEntityByProjectIdAndNameContainsIgnoreCase(
                        taskState.getProject().getId(),
                        taskStateName)
                .filter(anotherTaskState -> !anotherTaskState.getId().equals(taskStateId))
                .ifPresent(anotherTaskState ->{
                    throw new BadRequestException("Task state already exists.");
                });

        taskState.setName(taskStateName);

        taskState = taskStateRepository.saveAndFlush(taskState);

        return taskStateDtoFactory.makeTaskStateDto(taskState);
    }

    private TaskStateEntity getTaskStateOrThrowException(Long taskStateId){
        return taskStateRepository
                .findById(taskStateId)
                .orElseThrow(()-> new NotFoundException("Task state not found"));
    }
}



