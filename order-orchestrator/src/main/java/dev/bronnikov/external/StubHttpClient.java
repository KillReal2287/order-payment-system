package dev.bronnikov.external;

import dev.bronnikov.api.payment.AuthorizePaymentRequestDto;
import dev.bronnikov.api.payment.AuthorizePaymentResponseDto;
import dev.bronnikov.api.payment.CapturePaymentRequestDto;
import dev.bronnikov.api.payment.CapturePaymentResponseDto;
import dev.bronnikov.api.warehouse.CalculatePricingRequestDto;
import dev.bronnikov.api.warehouse.CalculatePricingResponseDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(
        accept = MediaType.APPLICATION_JSON_VALUE,
        contentType = MediaType.APPLICATION_JSON_VALUE
)
public interface StubHttpClient {

    @PostExchange("/payment/authorize")
    AuthorizePaymentResponseDto authorizePayment(
            @RequestBody AuthorizePaymentRequestDto request
    );

    @PostExchange("/payment/capture")
    CapturePaymentResponseDto capturePayment(
            @RequestBody CapturePaymentRequestDto request
    );

    @PostExchange("/warehouse/calculate-price")
    CalculatePricingResponseDto calculatePricing(
            @RequestBody CalculatePricingRequestDto request
    );
}
