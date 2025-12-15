package conectaseguros.co.news_service.descuentos.infrastructure.output.payment;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** Configuración del cliente Feign para payments-service. */
@Configuration
public class PaymentsServiceClientConfig {

  /**
   * Interceptor para agregar token OAuth2 y headers de servicio interno a las peticiones Feign.
   */
  @Bean
  public RequestInterceptor oauth2FeignRequestInterceptor() {
    return requestTemplate -> {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      // Agregar token JWT si existe
      if (authentication instanceof JwtAuthenticationToken jwtAuth) {
        Jwt jwt = jwtAuth.getToken();
        requestTemplate.header("Authorization", "Bearer " + jwt.getTokenValue());
      }

      // Agregar headers de servicio interno para bypass de OAuth2
      requestTemplate.header("X-Request-Source", "internal");
      requestTemplate.header("X-Service-Name", "news-service");
    };
  }
}
