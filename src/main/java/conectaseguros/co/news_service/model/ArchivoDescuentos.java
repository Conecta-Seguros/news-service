package conectaseguros.co.news_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "archivos_descuentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchivoDescuentos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(nullable = false, length = 7)
    private String periodo;

    @Column(name = "seccional_id", nullable = false)
    private Long seccionalId;

    @Column(name = "total_registros", nullable = false)
    private Integer totalRegistros = 0;

    @Column(name = "registros_procesados", nullable = false)
    private Integer registrosProcesados = 0;

    @Column(name = "diferencias_encontradas", nullable = false)
    private Integer diferenciasEncontradas = 0;

    @Column(nullable = false, length = 20)
    private String estado = "PROCESANDO";

    @Column(name = "ruta_archivo", length = 500)
    private String rutaArchivo;

    @Column(name = "fecha_carga", updatable = false)
    private LocalDateTime fechaCarga;

    @Column(name = "usuario_carga", nullable = false)
    private Long usuarioCarga;

    @PrePersist
    protected void onCreate() {
        fechaCarga = LocalDateTime.now();
    }
}