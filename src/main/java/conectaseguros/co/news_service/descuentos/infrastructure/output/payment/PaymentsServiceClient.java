package conectaseguros.co.news_service.descuentos.infrastructure.output.payment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** Cliente Feign para comunicación con el payments-service. */
@FeignClient(
    name = "payments-service",
    fallback = PaymentsServiceClientFallback.class,
    configuration = PaymentsServiceClientConfig.class)
public interface PaymentsServiceClient {

  /**
   * Registra un pago de nómina.
   *
   * @param request Datos del pago a registrar
   * @return Respuesta con el pago creado
   */
  @PostMapping("/api/v1/payments/pagos/nomina")
  PaymentApiResponse<PagoResponseDTO> registrarPagoNomina(
      @RequestBody PagoNominaRequestDTO request);
}
