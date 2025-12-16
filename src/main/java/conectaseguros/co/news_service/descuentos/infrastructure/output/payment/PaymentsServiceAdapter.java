package conectaseguros.co.news_service.descuentos.infrastructure.output.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Adapter para comunicación con payments-service usando WebClient con Service Discovery.
 */
@Component
public class PaymentsServiceAdapter {

  private static final Logger logger = LoggerFactory.getLogger(PaymentsServiceAdapter.class);

  private final WebClient.Builder webClientBuilder;
  private final String serviceName;

  public PaymentsServiceAdapter(
      WebClient.Builder webClientBuilder,
      @Value("${app.services.payments-service.name:payments-service}") String serviceName) {
    this.webClientBuilder = webClientBuilder;
    this.serviceName = serviceName;

    logger.info("✅ PaymentsServiceAdapter configurado:");
    logger.info("   - Servicio: {}", serviceName);
  }

  /**
   * Registra un pago de nómina en payments-service.
   *
   * @param request Datos del pago a registrar
   * @return Respuesta con el pago creado
   */
  public PaymentApiResponse<PagoResponseDTO> registrarPagoNomina(PagoNominaRequestDTO request) {
    try {
      logger.debug("=== REGISTRANDO PAGO EN PAYMENTS-SERVICE CON WEBCLIENT ===");
      logger.debug("Request: {}", request);

      String url = "lb://" + serviceName + "/api/v1/payments/pagos/nomina";
      logger.debug("URL con Eureka Discovery: {}", url);

      PaymentApiResponse<PagoResponseDTO> response =
          webClientBuilder
              .build()
              .post()
              .uri(url)
              .header("Accept", "application/json")
              .header("Content-Type", "application/json")
              .header("X-Service-Name", "news-service")
              .header("X-Request-Source", "internal")
              .bodyValue(request)
              .retrieve()
              .bodyToMono(new ParameterizedTypeReference<PaymentApiResponse<PagoResponseDTO>>() {})
              .block();

      logger.debug("=== RESPONSE DE PAYMENTS-SERVICE ===");
      logger.debug("Response: {}", response);

      if (response == null) {
        logger.error("❌ Response nulo de payments-service");
        throw new PaymentsServiceException("Response nulo de payments-service");
      }

      logger.info(
          "✅ Pago registrado exitosamente en payments-service - ID: {}",
          response.getData() != null ? response.getData().getId() : "N/A");

      return response;

    } catch (WebClientResponseException e) {
      logger.error(
          "❌ Error WebClient {} al registrar pago en payments-service: {}",
          e.getStatusCode(),
          e.getResponseBodyAsString());

      throw new PaymentsServiceException(
          "Error HTTP " + e.getStatusCode() + ": " + e.getResponseBodyAsString(), e);

    } catch (Exception e) {
      logger.error("❌ Error al comunicarse con payments-service: {}", e.getMessage(), e);
      throw new PaymentsServiceException(
          "Error de comunicación con payments-service: " + e.getMessage(), e);
    }
  }

  /** Excepción personalizada para errores del servicio de pagos */
  public static class PaymentsServiceException extends RuntimeException {
    public PaymentsServiceException(String mensaje) {
      super(mensaje);
    }

    public PaymentsServiceException(String mensaje, Throwable causa) {
      super(mensaje, causa);
    }
  }
}
