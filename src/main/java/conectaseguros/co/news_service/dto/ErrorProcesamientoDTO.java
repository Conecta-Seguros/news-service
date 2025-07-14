package conectaseguros.co.news_service.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorProcesamientoDTO {
    private Long id;
    private Long archivoId;
    private String tipoArchivo;
    private Integer lineaArchivo;
    private String cedulaCliente;
    private String tipoError;
    private String descripcionError;
    private LocalDateTime fechaError;
}
