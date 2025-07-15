package conectaseguros.co.news_service.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeProcesamientoDTO {
    private Integer numeroLinea;
    private String cedulaCliente;
    private String numeroPoliza;
    private String tipoMensaje; // EXITO, ERROR, ADVERTENCIA
    private String mensaje;
    private String detalle;
    private String nombreCliente;
    private String productoNombre;
}