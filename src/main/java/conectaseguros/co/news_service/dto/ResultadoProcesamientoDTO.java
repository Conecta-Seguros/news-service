package conectaseguros.co.news_service.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoProcesamientoDTO {
    private String nombreArchivo;
    private Integer totalRegistros;
    private Integer registrosExitosos;
    private Integer registrosErrores;
    private String estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String duracionProcesamiento;
    private List<MensajeProcesamientoDTO> mensajesExito;
    private List<MensajeProcesamientoDTO> mensajesError;
    private String resumenProcesamiento;
}