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
    private Long aseguradoraId;
    private Long seccionalId;
    private String descripcion;
    private BigDecimal cuota;
    private BigDecimal valor;
    private String nomina;
    private LocalDate fecha;
    private String periodo;
    private String estado;
    private Long archivoId;
    private LocalDateTime fechaCreacion;
}

