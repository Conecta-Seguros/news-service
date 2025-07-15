package conectaseguros.co.news_service.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovedadArchivoDTO {
    private String cedulaCliente;
    private String numeroPoliza;
    private String tipoNovedad;
    private BigDecimal valor;
    private BigDecimal porcentaje;
    private String descripcion;
    private LocalDate fechaAplicacion;
    private LocalDate fechaVencimiento;
    private Integer numeroLinea;
}
