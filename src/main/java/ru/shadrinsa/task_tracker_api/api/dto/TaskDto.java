package ru.shadrinsa.task_tracker_api.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.*;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskDto {
    private Long id;

    private String name;
    private String description;

    @JsonProperty("created_at")
    private Instant createdAt;
}
