package conectaseguros.co.news_service.descuentos.infrastructure.output.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

/** DTO para registrar un pago de nómina en el payments-service. */
@Value
@Builder
public class PagoNominaRequestDTO {

  Long polizaId;
  Long productoClienteId;
  Long clienteId;
  Long archivoNominaId;
  BigDecimal valorEsperado;
  BigDecimal valorPagado;
  Integer periodoYear;
  Integer periodoMonth;
  Integer numeroCuota;
  LocalDate fechaPago;
  String observaciones;
}
