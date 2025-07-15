package conectaseguros.co.news_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "procesamiento_archivos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcesamientoArchivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_archivo", nullable = false, length = 200)
    private String nombreArchivo;

    @Column(name = "total_registros", nullable = false)
    private Integer totalRegistros;

    @Column(name = "registros_exitosos", nullable = false)
    private Integer registrosExitosos;

    @Column(name = "registros_errores", nullable = false)
    private Integer registrosErrores;

    @Column(nullable = false, length = 20)
    private String estado; // PROCESANDO, COMPLETADO, ERROR

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "usuario_procesamiento", length = 100)
    private String usuarioProcesamiento;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @PrePersist
    protected void onCreate() {
        if (fechaInicio == null) {
            fechaInicio = LocalDateTime.now();
        }
    }
}
