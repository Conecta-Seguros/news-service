package conectaseguros.co.news_service.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DescuentoDTO {
    private Long id;
    private Long clienteId;
    private Long seccionalId;
    private LocalDate fecha;
    private BigDecimal valor;
    private BigDecimal valorEsperado;
    private BigDecimal diferencia;
    private String estado;
    private Long archivoId;
    private LocalDateTime fechaCreacion;
}
