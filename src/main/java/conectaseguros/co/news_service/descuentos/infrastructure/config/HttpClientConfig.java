package conectaseguros.co.news_service.descuentos.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class HttpClientConfig {

    @Value("${app.services.timeout.connection:5}")
    private int connectionTimeoutSeconds;

    @Value("${app.services.timeout.read:10}")
    private int readTimeoutSeconds;

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(connectionTimeoutSeconds));
        factory.setConnectionRequestTimeout(Duration.ofSeconds(readTimeoutSeconds));

        return new RestTemplate(factory);
    }
}
