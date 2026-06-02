package dev.bronnikov.external;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@EnableConfigurationProperties(StubHttpClientProperties.class)
public class StubHttpClientConfig {

    @Bean
    StubHttpClient stubHttpClient(
            RestClient.Builder restClientBuilder,
            StubHttpClientProperties properties
    ) {
        var restClient = restClientBuilder
                .baseUrl(properties.getBaseUrl().toString())
                .build();

        var adapter = RestClientAdapter.create(restClient);

        return HttpServiceProxyFactory
                .builderFor(adapter)
                .build()
                .createClient(StubHttpClient.class);
    }
}
