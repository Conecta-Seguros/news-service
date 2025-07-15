package conectaseguros.co.news_service.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovedadDTO {
    private Long id;
    private Long clienteId;
    private String cedulaCliente;
    private String nombreCliente;
    private String numeroPoliza;
    private String productoNombre;
    private String tipoNovedad;
    private BigDecimal valor;
    private BigDecimal porcentaje;
    private String descripcion;
    private LocalDate fechaAplicacion;
    private LocalDate fechaVencimiento;
    private String estado;
    private String archivoOrigen;
    private Integer numeroLinea;
    private LocalDateTime fechaCreacion;
    private String usuarioCreacion;
}

