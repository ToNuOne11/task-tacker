package ru.shadrinsa.task_tracker_api.api.factories;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.shadrinsa.task_tracker_api.api.dto.ProjectDto;
import ru.shadrinsa.task_tracker_api.store.entities.ProjectEntity;

@Component
public class ProjectDtoFactory  {
    public ProjectDto makeProjectDto(ProjectEntity entity){
        return ProjectDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreateAt())
                .build();
    }
}
