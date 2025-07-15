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
    private Integer clienteId;

    @Column(name = "aseguradora_id", nullable = false)
    private Integer aseguradoraId;

    @Column(name = "seccional_id", nullable = false)
    private Integer seccionalId;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "cuota", nullable = false, precision = 10, scale = 2)
    private BigDecimal cuota;

    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "nomina", length = 100)
    private String nomina;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "periodo", nullable = false, length = 7)
    private String periodo;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(name = "archivo_id")
    private Integer archivoId;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}