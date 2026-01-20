package conectaseguros.co.news_service.descuentos.infrastructure.output.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Fallback para el cliente Feign de payments-service. */
@Component
public class PaymentsServiceClientFallback implements PaymentsServiceClient {

  private static final Logger logger = LoggerFactory.getLogger(PaymentsServiceClientFallback.class);

  @Override
  public PaymentApiResponse<PagoResponseDTO> registrarPagoNomina(PagoNominaRequestDTO request) {
    logger.error(
        "Fallback activado para registrarPagoNomina - payments-service no disponible. Cliente: {}, Periodo: {}-{}",
        request.getClienteId(),
        request.getPeriodoYear(),
        request.getPeriodoMonth());

    throw new RuntimeException(
        "payments-service no está disponible. No se pudo registrar el pago para cliente: "
            + request.getClienteId());
  }
}
