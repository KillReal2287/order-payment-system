package dev.bronnikov.engine;

import dev.bronnikov.api.payment.AuthorizationStatus;
import dev.bronnikov.api.payment.AuthorizePaymentRequestDto;
import dev.bronnikov.api.payment.AuthorizePaymentResponseDto;
import dev.bronnikov.api.payment.CapturePaymentRequestDto;
import dev.bronnikov.api.payment.CaptureStatus;
import dev.bronnikov.api.warehouse.CalculatePricingRequestDto;
import dev.bronnikov.api.warehouse.CalculatePricingResponseDto;
import dev.bronnikov.domain.OrderEntity;
import dev.bronnikov.domain.OrderRepository;
import dev.bronnikov.domain.OrderService;
import dev.bronnikov.domain.PaymentStatus;
import dev.bronnikov.domain.TaskEntity;
import dev.bronnikov.domain.TaskStatus;
import dev.bronnikov.external.StubHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskProcessor {
    private final StubHttpClient stubClient;
    private final ExecutorService executorService;

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public TaskStatus handleTask(TaskEntity task) {
        Optional<OrderEntity> orderEntity = orderService.findOrder(task.getOrderId());
        if (orderEntity.isEmpty()) {
            log.error("No order found for orderId={} and task is failedNonRetryable", task.getOrderId());
            return TaskStatus.FAILED_NON_RETRYABLE;
        }

        OrderEntity order = orderEntity.get();
        AuthorizePaymentRequestDto authorizeRequest = new AuthorizePaymentRequestDto(1L, order.getClientEstimate());
        var authorizeResponse = stubClient.authorizePayment(authorizeRequest);
        order.setAuthorizedAmount(authorizeResponse.authorizedAmount());
        orderRepository.save(order);
        if (authorizeResponse.status().equals(AuthorizationStatus.DECLINED)) {
            log.error("The card was declined, order {}", order);
            orderService.cancelOrder(order, authorizeResponse.message(), PaymentStatus.AUTHORIZATION_FAILED);
            return TaskStatus.FAILED_NON_RETRYABLE;
        }

        try {
            return CompletableFuture.supplyAsync(() -> makeRepricing(order), executorService)
                    .thenApplyAsync((price) -> handlePriceAndCapture(price, authorizeResponse, order),
                            executorService)
                    .exceptionally(ex -> {
                        log.error("Error handling order price and capture", ex);
                        return TaskStatus.FAILED_RETRYABLE;
                    })
                    .get();
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for repricing or capturing", e);
            Thread.currentThread().interrupt();
            return TaskStatus.FAILED_RETRYABLE;

        } catch (ExecutionException e) {
            log.error("An error occurred while waiting for smth", e);
            return TaskStatus.FAILED_RETRYABLE;
        }
    }


    private CalculatePricingResponseDto makeRepricing(OrderEntity order) {
        log.info("I am making repricing for order {}", order);
        return stubClient.calculatePricing(new CalculatePricingRequestDto(order.getId()));
    }

    private TaskStatus handlePriceAndCapture(CalculatePricingResponseDto priceResponse, AuthorizePaymentResponseDto authorizeResponse
    , OrderEntity order) {
        if (priceResponse.finalAmount().compareTo(authorizeResponse.authorizedAmount()) > 0) {
            log.error("Произошел перерасчет для order {}!", order);
            order.setFinalAmount(priceResponse.finalAmount());
            orderService.cancelOrder(order, "The payment has been changed", PaymentStatus.PRICE_CHANGED_FAILED);
            return TaskStatus.FAILED_NON_RETRYABLE;
        }
        var captureResponse = stubClient.capturePayment(new CapturePaymentRequestDto(priceResponse.finalAmount(), 1L));
        if (captureResponse.status().equals(CaptureStatus.FAILED)) {
            log.error("Невозможно списать средства, по причине {}", captureResponse.message());
            orderService.cancelOrder(order, "Money wasn't captured", PaymentStatus.CAPTURE_FAILED);
            return TaskStatus.FAILED_NON_RETRYABLE;
        }
        log.info("Средства списаны, финальное сохранение статусов таски и заказа");
        order.setPaymentStatus(PaymentStatus.SUCCEED_PAID);
        order.setFinalAmount(priceResponse.finalAmount());
        order.setCapturedAmount(captureResponse.capturedAmount());
        orderRepository.save(order);
        return TaskStatus.SUCCEEDED;
    }

}
