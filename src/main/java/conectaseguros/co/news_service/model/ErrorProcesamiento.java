package conectaseguros.co.news_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "errores_procesamiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorProcesamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "archivo_id", nullable = false)
    private Long archivoId;

    @Column(name = "tipo_archivo", nullable = false, length = 20)
    private String tipoArchivo;

    @Column(name = "linea_archivo")
    private Integer lineaArchivo;

    @Column(name = "cedula_cliente", length = 20)
    private String cedulaCliente;

    @Column(name = "tipo_error", nullable = false, length = 50)
    private String tipoError;

    @Column(name = "descripcion_error", columnDefinition = "TEXT")
    private String descripcionError;

    @Column(name = "fecha_error", updatable = false)
    private LocalDateTime fechaError;

    @PrePersist
    protected void onCreate() {
        fechaError = LocalDateTime.now();
    }
}
