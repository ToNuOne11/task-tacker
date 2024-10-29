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
    public static final String UPDATE_TASK_STATES = "/api/task-state/{task_state_id}";
    public static final String CHANGE_TASK_STATE_POSITION = "/api/task-state/{task_state_id}/position/change";


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
            if (taskState.getRightTaskState().isEmpty()) {
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
                                        @RequestParam(name = "task_state_name") String taskStateName) {
        if (taskStateName.trim().isEmpty()) {
            throw new BadRequestException("Task state name is empty");
        }
        TaskStateEntity taskState = getTaskStateOrThrowException(taskStateId);
        taskStateRepository
                .findTaskStateEntityByProjectIdAndNameContainsIgnoreCase(
                        taskState.getProject().getId(),
                        taskStateName)
                .filter(anotherTaskState -> !anotherTaskState.getId().equals(taskStateId))
                .ifPresent(anotherTaskState -> {
                    throw new BadRequestException("Task state already exists.");
                });

        taskState.setName(taskStateName);

        taskState = taskStateRepository.saveAndFlush(taskState);

        return taskStateDtoFactory.makeTaskStateDto(taskState);
    }

    @PatchMapping(CHANGE_TASK_STATE_POSITION)
    public TaskStateDto changeTaskStatePosition(@PathVariable(name = "task_state_id") Long taskStateId,
                                           @RequestParam(name = "left_task_state_id", required = false) Optional<Long> optionalLeftTaskStateId) {
        TaskStateEntity changeTaskState = getTaskStateOrThrowException(taskStateId);

        ProjectEntity project = changeTaskState.getProject();


        Optional<TaskStateEntity> optionalNewLeftTaskState = optionalLeftTaskStateId
                .map(leftTaskStateId -> {

                    if (taskStateId.equals(leftTaskStateId)) {
                        throw new BadRequestException("Left task state equals task state id.");
                    }

                    TaskStateEntity leftTaskStateEntity = getTaskStateOrThrowException(leftTaskStateId);

                    if (!project.getId().equals(leftTaskStateEntity.getProject().getId())) {
                        throw new BadRequestException("Task states in different projects");
                    }

                    return leftTaskStateEntity;
                });

        Optional<TaskStateEntity> optionalNewRightTaskState;
        if (optionalNewLeftTaskState.isEmpty()) {
            optionalNewRightTaskState = project
                    .getTaskStates()
                    .stream()
                    .filter(anotherTaskState -> anotherTaskState.getLeftTaskState().isEmpty())
                    .findAny();
        } else {
            optionalNewRightTaskState = optionalNewLeftTaskState
                    .get()
                    .getRightTaskState();
        }

        Optional<TaskStateEntity> optionalOldLeftTaskState = changeTaskState.getLeftTaskState();
        Optional<TaskStateEntity> optionalOldRightTaskState = changeTaskState.getRightTaskState();

        optionalOldLeftTaskState
                .ifPresent(it -> {
                    it.setRightTaskState(optionalOldRightTaskState.orElse(null));
                    taskStateRepository.saveAndFlush(it);
                });

        optionalOldRightTaskState
                .ifPresent(it -> {
                    it.setLeftTaskState(optionalOldLeftTaskState.orElse(null));
                    taskStateRepository.saveAndFlush(it);
                });

        if(optionalNewLeftTaskState.isPresent()){
            TaskStateEntity newLeftTaskState = optionalNewLeftTaskState.get();
            newLeftTaskState.setRightTaskState(changeTaskState);
            changeTaskState.setLeftTaskState(newLeftTaskState);

        }
        else {
            changeTaskState.setLeftTaskState(null);
        }
        if(optionalNewRightTaskState.isPresent()){
            TaskStateEntity newRightTaskState = optionalNewRightTaskState.get();
            newRightTaskState.setLeftTaskState(changeTaskState);
            changeTaskState.setRightTaskState(newRightTaskState);
        }
        else {
            changeTaskState.setRightTaskState(null);
        }
        changeTaskState = taskStateRepository.saveAndFlush(changeTaskState);

        optionalNewLeftTaskState
                .ifPresent(taskStateRepository::saveAndFlush);
        optionalNewRightTaskState
                .ifPresent(taskStateRepository::saveAndFlush);

        return taskStateDtoFactory.makeTaskStateDto(changeTaskState);
    }

    private TaskStateEntity getTaskStateOrThrowException(Long taskStateId) {
        return taskStateRepository
                .findById(taskStateId)
                .orElseThrow(() -> new NotFoundException("Task state not found"));
    }
}



