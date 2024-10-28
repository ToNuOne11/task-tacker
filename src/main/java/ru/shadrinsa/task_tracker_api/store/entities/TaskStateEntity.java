package ru.shadrinsa.task_tracker_api.store.entities;

import jakarta.persistence.*;
import lombok.*;
import ru.shadrinsa.task_tracker_api.api.dto.TaskStateDto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "task_state")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskStateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    @OneToOne
    TaskStateEntity leftTaskState;
    @OneToOne
    TaskStateEntity rightTaskState;
    @Builder.Default
    private Instant createdAt = Instant.now();
    private String description;

    @ManyToOne
    ProjectEntity project;
    @OneToMany
    @Builder.Default
    @JoinColumn(name = "task_state_id", referencedColumnName = "id")
    private List<TaskEntity> tasks = new ArrayList<>();

    public Optional<TaskStateEntity> getLeftTaskState(){
        return Optional.ofNullable(leftTaskState);
    }
    public Optional<TaskStateEntity> getRightTaskState(){
        return Optional.ofNullable(rightTaskState);
    }
}
