package ru.shadrinsa.task_tracker_api.api.factories;

import org.springframework.stereotype.Component;
import ru.shadrinsa.task_tracker_api.api.dto.TaskStateDto;
import ru.shadrinsa.task_tracker_api.store.entities.TaskStateEntity;

@Component
public class TaskStateDtoFactory {
    TaskStateDto makeTaskStateDto(TaskStateEntity entity){
        return TaskStateDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .ordinal(entity.getOrdinal())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
