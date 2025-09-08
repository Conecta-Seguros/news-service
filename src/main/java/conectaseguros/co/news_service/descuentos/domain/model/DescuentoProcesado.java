package conectaseguros.co.news_service.descuentos.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa un descuento ya procesado
 */
public class DescuentoProcesado {

    private final Long id;
    private final Integer clienteId;
    private final Integer seccionalId;
    private final Integer aseguradoraId;
    private final String cedulaCliente;
    private final String periodo;
    private final LocalDate fecha;
    private final String codigoDescuento;
    private final String codigoValidacion;
    private final BigDecimal valor;
    private final BigDecimal valorEsperado;
    private final BigDecimal diferencia;
    private final String estado;
    private final Long archivoId;
    private final LocalDateTime fechaCreacion;

    public DescuentoProcesado(Long id, Integer clienteId, Integer seccionalId, Integer aseguradoraId,
                              String cedulaCliente, String periodo, LocalDate fecha, String codigoDescuento,
                              String codigoValidacion, BigDecimal valor, BigDecimal valorEsperado,
                              BigDecimal diferencia, String estado, Long archivoId, LocalDateTime fechaCreacion) {
        this.id = id;
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
        this.diferencia = diferencia;
        this.estado = estado;
        this.archivoId = archivoId;
        this.fechaCreacion = fechaCreacion;
    }

    // Getters
    public Long getId() { return id; }
    public Integer getClienteId() { return clienteId; }
    public Integer getSeccionalId() { return seccionalId; }
    public Integer getAseguradoraId() { return aseguradoraId; }
    public String getCedulaCliente() { return cedulaCliente; }
    public String getPeriodo() { return periodo; }
    public LocalDate getFecha() { return fecha; }
    public String getCodigoDescuento() { return codigoDescuento; }
    public String getCodigoValidacion() { return codigoValidacion; }
    public BigDecimal getValor() { return valor; }
    public BigDecimal getValorEsperado() { return valorEsperado; }
    public BigDecimal getDiferencia() { return diferencia; }
    public String getEstado() { return estado; }
    public Long getArchivoId() { return archivoId; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}
