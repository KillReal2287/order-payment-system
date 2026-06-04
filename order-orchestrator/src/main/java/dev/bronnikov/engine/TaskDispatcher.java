package dev.bronnikov.engine;

import dev.bronnikov.config.OrderPaymentSystemConfig;
import dev.bronnikov.domain.OrderEntity;
import dev.bronnikov.domain.OrderService;
import dev.bronnikov.domain.PaymentStatus;
import dev.bronnikov.domain.TaskEntity;
import dev.bronnikov.domain.TaskRepository;
import dev.bronnikov.domain.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskDispatcher {
    private final TaskProcessor taskProcessor;
    private final OrderPaymentSystemConfig orderPaymentSystemConfig;
    private final TaskRepository taskRepository;
    private final OrderService orderService;
    private final ExecutorService executorService;

    public void dispatchTask(TaskEntity task) {
        CompletableFuture.supplyAsync(() -> taskProcessor.handleTask(task), executorService)
                .thenAccept(result -> handleTaskResult(result, task))
                .exceptionally(ex -> handleException(ex, task));
    }

    private void handleTaskResult(TaskStatus result, TaskEntity task) {
        switch (result) {
            case FAILED_RETRYABLE -> retryTask(task);
            case SUCCEEDED -> updateTaskStatusAndSave(TaskStatus.SUCCEEDED, task);
            case FAILED_NON_RETRYABLE -> updateTaskStatusAndSave(TaskStatus.FAILED_NON_RETRYABLE, task);
        }
    }

    private void retryTask(TaskEntity task) {
        int attempts = task.getAttempts()+1;
        if (attempts >= orderPaymentSystemConfig.getMaxAttemptsForBlocking()){
            log.error("Таска уже заблочена, тут надо завершать её окончательно {}", task);
            updateTaskStatusAndSave(TaskStatus.FAILED_NON_RETRYABLE, task);
            Optional<OrderEntity> orderEntity = orderService.findOrder(task.getOrderId());
            orderEntity.ifPresent(order -> orderService.cancelOrder(order, "A lot of attempts were made to fix it!", PaymentStatus.RETRIES_FAILED));
        }
         else {
            task.setAttempts(attempts);
            task.setNextAttemptAt(OffsetDateTime.now().plusSeconds(attempts * 5L));
            updateTaskStatusAndSave(TaskStatus.FAILED_RETRYABLE, task);
        }
    }

    private void updateTaskStatusAndSave(TaskStatus taskStatus, TaskEntity task) {
        task.setStatus(taskStatus);
        taskRepository.save(task);
    }

    private Void handleException(Throwable ex, TaskEntity task) {
        log.error("Error while executing task {}", task,  ex);
        retryTask(task);
        return null;
    }
}
