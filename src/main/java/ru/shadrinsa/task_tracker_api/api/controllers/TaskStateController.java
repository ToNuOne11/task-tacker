package ru.shadrinsa.task_tracker_api.api.controllers;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.shadrinsa.task_tracker_api.api.controllers.helpers.ControllerHelper;
import ru.shadrinsa.task_tracker_api.api.dto.TaskStateDto;
import ru.shadrinsa.task_tracker_api.api.exeptions.NotFoundException;
import ru.shadrinsa.task_tracker_api.api.factories.TaskStateDtoFactory;
import ru.shadrinsa.task_tracker_api.store.entities.ProjectEntity;
import ru.shadrinsa.task_tracker_api.store.repositories.ProjectRepository;
import ru.shadrinsa.task_tracker_api.store.repositories.TaskStateRepository;

import java.util.List;

@Transactional
@RestController
@RequiredArgsConstructor
public class TaskStateController {

    private final TaskStateRepository taskStateRepository;
    private final TaskStateDtoFactory taskStateDtoFactory;
    private final ControllerHelper controllerHelper;

    public static final String GET_TASK_STATES = "/api/projects/{project_id}/task-states";


    @GetMapping(GET_TASK_STATES)
    public List<TaskStateDto> getTaskStates(@PathVariable(name = "project_id") Long projectId){

        ProjectEntity project = controllerHelper.getProjectOrThrowException(projectId);

        return project
                .getTaskStates()
                .stream()
                .map(taskStateDtoFactory::makeTaskStateDto)
                .toList();
    }

}
