package ru.shadrinsa.task_tracker_api.api.exeptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDTO {
    private String error;
    @JsonProperty("error_description")
    String errorDescription;
}
