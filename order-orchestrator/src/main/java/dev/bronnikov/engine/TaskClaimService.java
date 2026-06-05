package dev.bronnikov.engine;

import dev.bronnikov.domain.TaskEntity;
import dev.bronnikov.domain.TaskRepository;
import dev.bronnikov.domain.TaskStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskClaimService {
    private final TaskRepository taskRepository;

    @Transactional
    public List<TaskEntity> claimTasks() {
        var tasks = taskRepository.findByStatuses(TaskStatus.NEW.name(), TaskStatus.FAILED_RETRYABLE.name(), OffsetDateTime.now());
        tasks.forEach(task -> task.setStatus(TaskStatus.IN_PROGRESS));
        return taskRepository.saveAll(tasks);
    }
}
