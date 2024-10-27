package ru.shadrinsa.task_tracker_api.api.factories;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.shadrinsa.task_tracker_api.api.dto.TaskStateDto;
import ru.shadrinsa.task_tracker_api.store.entities.TaskStateEntity;

@RequiredArgsConstructor
@Component
public class TaskStateDtoFactory {
    private final TaskDtoFactory taskDtoFactory;
    public TaskStateDto makeTaskStateDto(TaskStateEntity entity){
        return TaskStateDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .ordinal(entity.getOrdinal())
                .createdAt(entity.getCreatedAt())
                .tasks(
                        entity
                                .getTasks()
                                .stream()
                                .map(taskDtoFactory::makeTaskDto)
                                .toList()
                )
                .build();
    }
}
