package conectaseguros.co.news_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "novedades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Novedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "aseguradora_id", nullable = false)
    private Long aseguradoraId;

    @Column(name = "seccional_id", nullable = false)
    private Long seccionalId;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cuota;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(length = 100)
    private String nomina;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 7)
    private String periodo;

    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(name = "archivo_id")
    private Long archivoId;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}
