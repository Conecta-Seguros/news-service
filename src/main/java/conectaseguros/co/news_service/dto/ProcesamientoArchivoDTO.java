package conectaseguros.co.news_service.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcesamientoArchivoDTO {
    private Long id;
    private String nombreArchivo;
    private Integer totalRegistros;
    private Integer registrosExitosos;
    private Integer registrosErrores;
    private String estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String usuarioProcesamiento;
    private String observaciones;
    private String duracionProcesamiento;
}
