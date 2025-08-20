package conectaseguros.co.news_service.descuentos.infrastructure.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpClientConfig {

    /**
     * RestTemplate básico SIN interceptors para evitar dependencia circular
     */
    @Bean("basicRestTemplate")
    public RestTemplate basicRestTemplate() {
        return createRestTemplate();
    }

    /**
     * RestTemplate principal CON interceptors para autenticación automática
     */
    @Bean
    @Primary
    public RestTemplate restTemplate() {
        return createRestTemplate();
    }

    private RestTemplate createRestTemplate() {
        // ✅ NUEVO: Configurar HttpClient para seguir redirects
        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder
                .create()
                .setMaxConnTotal(20)
                .setMaxConnPerRoute(10)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(
                        org.apache.hc.client5.http.config.RequestConfig.custom()
                                .setConnectTimeout(Timeout.ofSeconds(5))
                                .setResponseTimeout(Timeout.ofSeconds(10))
                                .setRedirectsEnabled(true)          // ✅ HABILITAR REDIRECTS
                                .setMaxRedirects(3)                 // ✅ MÁXIMO 3 redirects
                                .setCircularRedirectsAllowed(false) // ✅ NO permitir redirects circulares
                                .build()
                )
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(factory);
    }
}