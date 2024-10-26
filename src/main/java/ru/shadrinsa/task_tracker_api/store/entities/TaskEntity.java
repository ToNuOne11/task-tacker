package ru.shadrinsa.task_tracker_api.store.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;


@Entity
@Table(name = "task")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", columnDefinition = "text")
    private String name;
    @Column(name = "descriptional", columnDefinition = "text")
    private String description;
    @Builder.Default
    private Instant createdAt = Instant.now();


}
