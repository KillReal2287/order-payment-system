package dev.bronnikov.api;

import java.math.BigDecimal;

public record OrderCreateRequestDto(
        String address,
        BigDecimal clientEstimate
) { }
