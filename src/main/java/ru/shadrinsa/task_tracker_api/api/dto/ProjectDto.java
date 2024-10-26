package ru.shadrinsa.task_tracker_api.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDto {
    @NonNull
    private Long id;
    @NonNull
    private String name;
    @NonNull
    @JsonProperty("created_at")
    private Instant createdAt;
}
