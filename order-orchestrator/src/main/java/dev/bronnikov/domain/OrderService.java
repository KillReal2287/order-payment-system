package dev.bronnikov.domain;

import dev.bronnikov.api.OrderCreateRequestDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public OrderEntity createOrder(
            OrderCreateRequestDto requestDto
    ) {
        var order = OrderEntity.builder()
                .address(requestDto.address())
                .clientEstimate(requestDto.clientEstimate())
                .build();

        var orderEntity = orderRepository.save(order);
        var task = TaskEntity.builder()
                .orderId(orderEntity.getId())
                .status(TaskStatus.NEW)
                .build();
        taskRepository.save(task);
        return orderEntity;
    }

    public void cancelOrder(OrderEntity orderEntity, String reason, PaymentStatus paymentStatus) {
        orderEntity.setFailureReason(reason);
        orderEntity.setPaymentStatus(paymentStatus);
        orderRepository.save(orderEntity);
    }

    public Optional<OrderEntity> findOrder(UUID id) {
        return orderRepository.findById(id);
    }
}
