package dev.bronnikov.engine;

import dev.bronnikov.domain.TaskEntity;
import dev.bronnikov.domain.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
class TaskDispatcher {
    private final TaskService taskService;

    public void dispatchTask(TaskEntity task) {
        CompletableFuture.supplyAsync(() -> taskService.handleTask(task))
                .thenAccept(result -> handleTaskResult(result, task))
                .exceptionally(ex -> handleException());
    }

    private void handleTaskResult(TaskStatus result, TaskEntity task) {
    }

    private Void handleException() {
        return null;
    }
}
