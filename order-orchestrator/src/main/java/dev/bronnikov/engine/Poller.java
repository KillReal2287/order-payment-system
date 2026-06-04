package dev.bronnikov.engine;

import dev.bronnikov.domain.TaskEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class Poller {
    private final TaskClaimService taskClaimService;
    private final TaskDispatcher taskDispatcher;

    @Scheduled(fixedRate = 5000)
    public void pollTasks(){
        log.info("I am polling tasks");
        List<TaskEntity> tasks = taskClaimService.claimTasks();
        log.info("Found {} tasks", tasks.size());
        for (TaskEntity task : tasks){
            taskDispatcher.dispatchTask(task);
        }
    }
}
