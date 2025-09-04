package conectaseguros.co.news_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "productos_cliente")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoCliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;
    
    @Column(name = "poliza_id", nullable = false)
    private Long polizaId;
    
    @Column(name = "asegurado_id", nullable = false)
    private Long aseguradoId;
    
    @Column(name = "estado", nullable = false)
    private String estado;
    
    @Column(name = "valor_anual", precision = 19, scale = 2)
    private BigDecimal valorAnual;
    
    @Column(name = "valor_cuota_mensual", precision = 19, scale = 2)
    private BigDecimal valorCuotaMensual;
    
    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;
    
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;
    
    @Column(name = "fecha_cancelacion")
    private LocalDate fechaCancelacion;
    
    @Column(name = "motivo_cancelacion")
    private String motivoCancelacion;
}