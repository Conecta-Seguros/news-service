package conectaseguros.co.news_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "descuentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Descuento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "seccional_id", nullable = false)
    private Long seccionalId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "valor_esperado", precision = 10, scale = 2)
    private BigDecimal valorEsperado;

    @Column(precision = 10, scale = 2)
    private BigDecimal diferencia = BigDecimal.ZERO;

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
