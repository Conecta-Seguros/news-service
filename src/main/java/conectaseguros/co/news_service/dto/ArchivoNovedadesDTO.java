package conectaseguros.co.news_service.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchivoNovedadesDTO {
    private Long id;
    private String nombreArchivo;
    private String periodo;
    private Long seccionalId;
    private Integer totalRegistros;
    private Integer registrosExitosos;
    private Integer registrosErrores;
    private String estado;
    private String rutaArchivo;
    private LocalDateTime fechaCarga;
    private Long usuarioCarga;
}