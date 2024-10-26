package ru.shadrinsa.task_tracker_api.api.factories;

import org.springframework.stereotype.Component;
import ru.shadrinsa.task_tracker_api.api.dto.TaskDto;
import ru.shadrinsa.task_tracker_api.store.entities.TaskEntity;

@Component
public class TaskDtoFactory {
    TaskDto makeTaskDto(TaskEntity entity){
        return TaskDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .description(entity.getDescription())
                .build();
    }
}
