package dev.bronnikov.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "order-payment-system")
public class OrderPaymentSystemConfig {

    private int maxAttemptsForBlocking;
}
