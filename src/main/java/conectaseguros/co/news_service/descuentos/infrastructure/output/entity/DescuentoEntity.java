package conectaseguros.co.news_service.descuentos.infrastructure.output.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "descuentos")
public class DescuentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Integer clienteId;

    @Column(name = "seccional_id", nullable = false)
    private Integer seccionalId;

    @Column(name = "aseguradora_id")
    private Integer aseguradoraId;

    @Column(name = "cedula_cliente", nullable = false, length = 20)
    private String cedulaCliente;

    @Column(name = "periodo", nullable = false, length = 7)
    private String periodo;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "codigo_descuento", nullable = false, length = 50)
    private String codigoDescuento;

    @Column(name = "codigo_validacion", length = 50)
    private String codigoValidacion;

    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "valor_esperado", precision = 10, scale = 2)
    private BigDecimal valorEsperado;

    @Column(name = "diferencia", precision = 10, scale = 2, nullable = false)
    private BigDecimal diferencia = BigDecimal.ZERO;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(name = "archivo_id")
    private Long archivoId;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    // Constructor vacío
    public DescuentoEntity() {}

    // Constructor completo
    public DescuentoEntity(Integer clienteId, Integer seccionalId, Integer aseguradoraId,
                           String cedulaCliente, String periodo, LocalDate fecha,
                           String codigoDescuento, String codigoValidacion, BigDecimal valor,
                           BigDecimal valorEsperado, Long archivoId) {
        this.clienteId = clienteId;
        this.seccionalId = seccionalId;
        this.aseguradoraId = aseguradoraId;
        this.cedulaCliente = cedulaCliente;
        this.periodo = periodo;
        this.fecha = fecha;
        this.codigoDescuento = codigoDescuento;
        this.codigoValidacion = codigoValidacion;
        this.valor = valor;
        this.valorEsperado = valorEsperado;
        this.diferencia = calcularDiferencia(valor, valorEsperado);
        this.archivoId = archivoId;
        this.fechaCreacion = LocalDateTime.now();
    }

    private BigDecimal calcularDiferencia(BigDecimal valor, BigDecimal valorEsperado) {
        if (valorEsperado == null) {
            return BigDecimal.ZERO;
        }
        return valor.subtract(valorEsperado);
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }

    public Integer getSeccionalId() { return seccionalId; }
    public void setSeccionalId(Integer seccionalId) { this.seccionalId = seccionalId; }

    public Integer getAseguradoraId() { return aseguradoraId; }
    public void setAseguradoraId(Integer aseguradoraId) { this.aseguradoraId = aseguradoraId; }

    public String getCedulaCliente() { return cedulaCliente; }
    public void setCedulaCliente(String cedulaCliente) { this.cedulaCliente = cedulaCliente; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getCodigoDescuento() { return codigoDescuento; }
    public void setCodigoDescuento(String codigoDescuento) { this.codigoDescuento = codigoDescuento; }

    public String getCodigoValidacion() { return codigoValidacion; }
    public void setCodigoValidacion(String codigoValidacion) { this.codigoValidacion = codigoValidacion; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) {
        this.valor = valor;
        this.diferencia = calcularDiferencia(valor, this.valorEsperado);
    }

    public BigDecimal getValorEsperado() { return valorEsperado; }
    public void setValorEsperado(BigDecimal valorEsperado) {
        this.valorEsperado = valorEsperado;
        this.diferencia = calcularDiferencia(this.valor, valorEsperado);
    }

    public BigDecimal getDiferencia() { return diferencia; }
    public void setDiferencia(BigDecimal diferencia) { this.diferencia = diferencia; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Long getArchivoId() { return archivoId; }
    public void setArchivoId(Long archivoId) { this.archivoId = archivoId; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    @Override
    public String toString() {
        return String.format("DescuentoEntity{id=%d, cliente='%s', periodo='%s', valor=%s}",
                id, cedulaCliente, periodo, valor);
    }
}
