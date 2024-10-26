package ru.shadrinsa.task_tracker_api.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.shadrinsa.task_tracker_api.store.entities.TaskStateEntity;

public interface TaskStateRepository extends JpaRepository<TaskStateEntity, Long> {
}
