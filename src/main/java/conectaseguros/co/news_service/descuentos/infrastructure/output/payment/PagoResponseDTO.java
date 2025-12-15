package conectaseguros.co.news_service.descuentos.infrastructure.output.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO para la respuesta de registro de pago desde payments-service. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponseDTO {

  private Long id;
  private Long polizaId;
  private Long productoClienteId;
  private Long clienteId;
  private String tipoPago;
  private String estado;
  private BigDecimal valorEsperado;
  private BigDecimal valorPagado;
  private BigDecimal valorAplicado;
  private BigDecimal saldoPendiente;
  private BigDecimal saldoAFavor;
  private Integer periodoYear;
  private Integer periodoMonth;
  private Integer numeroCuota;
  private LocalDate fechaPago;
  private Long archivoNominaId;
  private String observaciones;
}
