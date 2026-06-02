package dev.bronnikov.external;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@Getter
@Setter
@ConfigurationProperties(prefix = "external.payment-stub")
public class StubHttpClientProperties {

    private URI baseUrl;
}
