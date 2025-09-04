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

    @Column(name = "cedula_cliente", nullable = false)
    private String cedulaCliente;

    @Column(name = "numero_poliza", nullable = false)
    private String numeroPoliza;

    @Column(name = "tipo_novedad", nullable = false)
    private String tipoNovedad;

    @Column(name = "aseguradora_id")
    private Integer aseguradoraId;

    @Column(name = "seccional_id")
    private Integer seccionalId;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "cuota", precision = 10, scale = 2)
    private BigDecimal cuota;

    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "porcentaje", precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(name = "nomina", length = 100)
    private String nomina;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "fecha_aplicacion")
    private LocalDate fechaAplicacion;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "periodo", length = 7)
    private String periodo;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(name = "archivo_id")
    private Integer archivoId;

    @Column(name = "archivo_origen")
    private String archivoOrigen;

    @Column(name = "numero_linea")
    private Integer numeroLinea;

    @Column(name = "usuario_creacion")
    private String usuarioCreacion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}