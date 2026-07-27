package conectaseguros.co.news_service.common.config;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import conectaseguros.co.news_service.archivo.application.mapper.ArchivoMapper;
import conectaseguros.co.news_service.archivo.domain.port.input.CargarArchivoUseCase;
import conectaseguros.co.news_service.archivo.domain.port.input.LeerArchivoUseCase;
import conectaseguros.co.news_service.archivo.domain.port.input.ListarArchivosDescuentosUseCase;
import conectaseguros.co.news_service.archivo.infrastructure.input.rest.ArchivoController;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.jackson.JacksonEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.env.EnvironmentEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.info.InfoContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.info.InfoEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.servlet.ServletManagementContextAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifica que {@link SecurityConfig} permite acceso anónimo a los endpoints de actuator {@code
 * health} e {@code info} (requeridos por el healthcheck de Docker, ver GitHub issue #23), mientras
 * que el resto de endpoints — tanto de negocio como de actuator — siguen exigiendo autenticación,
 * incluyendo un JWT válido para los endpoints de actuator distintos de {@code health}/{@code info}.
 *
 * <p>Usa una configuración raíz mínima ({@link TestApplication}) en lugar de dejar que
 * {@code @WebMvcTest} auto-detecte {@code NewsServiceApplication}, ya que esta última declara
 * {@code @EnableJpaRepositories} directamente sobre sí misma. Eso forzaría la creación de proxies
 * de repositorio JPA (y por tanto de un {@code EntityManagerFactory} real) incluso dentro de un
 * slice test, algo que no es necesario para probar únicamente la cadena de seguridad.
 *
 * <p>La lista de exposición de endpoints replica la de {@code application-prod.properties} ({@code
 * health,info,metrics,prometheus,loggers,env}) para que la cobertura sea representativa de lo que
 * realmente está desplegado, en lugar de un endpoint arbitrario como {@code beans} que no se expone
 * en producción. Se usa {@code /actuator/env} como caso representativo de endpoint no público
 * porque solo requiere {@link EnvironmentEndpointAutoConfiguration}.
 *
 * <p>El caso "JWT válido" envía un header {@code Authorization: Bearer} real y stubea {@link
 * JwtDecoder} — en lugar de usar el atajo {@code SecurityMockMvcRequestPostProcessors.jwt()} — a
 * propósito: ese atajo inyecta la {@code Authentication} directamente en el {@code
 * SecurityContextHolder} antes de que el filtro de seguridad se ejecute, por lo que el request
 * pasaría como autenticado aunque {@code managementSecurityFilterChain} NO tuviera {@code
 * oauth2ResourceServer(...)} configurado (se verificó empíricamente: con ese atajo, el test seguía
 * en verde incluso reproduciendo el bug). Enviar el header real fuerza a que sea el filtro de esa
 * cadena concreta el que decodifique el token, que es precisamente lo que este bug rompía.
 */
@WebMvcTest(
    controllers = ArchivoController.class,
    properties =
        "management.endpoints.web.exposure.include=health,info,metrics,prometheus,loggers,env")
@ContextConfiguration(classes = SecurityConfigActuatorTest.TestApplication.class)
@Import({
  SecurityConfig.class,
  EndpointAutoConfiguration.class,
  WebEndpointAutoConfiguration.class,
  ServletManagementContextAutoConfiguration.class,
  ManagementContextAutoConfiguration.class,
  JacksonEndpointAutoConfiguration.class,
  HealthEndpointAutoConfiguration.class,
  HealthContributorAutoConfiguration.class,
  InfoEndpointAutoConfiguration.class,
  InfoContributorAutoConfiguration.class,
  EnvironmentEndpointAutoConfiguration.class
})
class SecurityConfigActuatorTest {

  @SpringBootConfiguration
  @EnableAutoConfiguration
  static class TestApplication {}

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CargarArchivoUseCase cargarArchivoUseCase;
  @MockitoBean private LeerArchivoUseCase leerArchivoUseCase;
  @MockitoBean private ListarArchivosDescuentosUseCase listarArchivosDescuentosUseCase;
  @MockitoBean private ArchivoMapper archivoMapper;
  @MockitoBean private JwtDecoder jwtDecoder;

  @Test
  void health_withoutAuth_returns200() throws Exception {
    mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
  }

  @Test
  void info_withoutAuth_returns200() throws Exception {
    mockMvc.perform(get("/actuator/info")).andExpect(status().isOk());
  }

  @Test
  void archivosEndpoint_withoutAuth_returns401() throws Exception {
    mockMvc.perform(get("/api/v1/news/archivos")).andExpect(status().isUnauthorized());
  }

  @Test
  void envEndpoint_withoutAuth_isNotPublic() throws Exception {
    // With oauth2ResourceServer wired on this chain, an unauthenticated request to a non-
    // health/info actuator endpoint is challenged by BearerTokenAuthenticationEntryPoint and
    // rejected as 401 — same as for business endpoints and matching the pre-split behavior.
    mockMvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
  }

  @Test
  void envEndpoint_withValidJwt_returns200() throws Exception {
    // The management filter chain must wire oauth2ResourceServer too, otherwise a request
    // matching EndpointRequest.toAnyEndpoint() is fully handled here and never falls through to
    // apiSecurityFilterChain's JWT decoder — making non-health/info actuator endpoints
    // unauthenticatable with any credentials, even a fully valid one.
    Jwt jwt =
        Jwt.withTokenValue("valid-token")
            .header("alg", "none")
            .subject("test-user")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .claim("realm_access", Map.of("roles", java.util.List.of()))
            .build();
    when(jwtDecoder.decode("valid-token")).thenReturn(jwt);

    mockMvc
        .perform(get("/actuator/env").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk());
  }
}
