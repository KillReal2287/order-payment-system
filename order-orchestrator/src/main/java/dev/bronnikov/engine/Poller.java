package dev.bronnikov.engine;

import dev.bronnikov.domain.TaskEntity;
import dev.bronnikov.domain.TaskRepository;
import dev.bronnikov.domain.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class Poller {
    private final TaskRepository taskRepository;
    private final TaskDispatcher taskDispatcher;

    @Scheduled(fixedRate = 5000)
    public void pollTasks(){
        log.info("I am polling tasks");
        List<TaskEntity> tasks = taskRepository.findByStatuses(
                TaskStatus.NEW.name(),
                TaskStatus.FAILED_RETRYABLE.name(),
                OffsetDateTime.now()
        );
        log.info("Found {} tasks", tasks.size());
        for (TaskEntity task : tasks){
            taskDispatcher.dispatchTask(task);
        }
    }
}
