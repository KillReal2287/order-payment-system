package dev.bronnikov.api;

import dev.bronnikov.domain.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record OrderDto(
        UUID id,
        String address,
        BigDecimal clientEstimate,
        BigDecimal finalAmount,
        BigDecimal authorizedAmount,
        BigDecimal capturedAmount,
        PaymentStatus paymentStatus,
        String failureReason
) { }
