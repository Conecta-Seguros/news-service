package conectaseguros.co.news_service.descuentos.infrastructure.input.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** DTO de respuesta para descuentos procesados */
public class DescuentoProcesadoDTO {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("cliente_id")
  private Integer clienteId;

  @JsonProperty("seccional_id")
  private Integer seccionalId;

  @JsonProperty("aseguradora_id")
  private Integer aseguradoraId;

  @JsonProperty("cedula_cliente")
  private String cedulaCliente;

  @JsonProperty("periodo")
  private String periodo;

  @JsonProperty("fecha")
  private String fecha;

  @JsonProperty("codigo_descuento")
  private String codigoDescuento;

  @JsonProperty("codigo_validacion")
  private String codigoValidacion;

  @JsonProperty("valor")
  private BigDecimal valor;

  @JsonProperty("valor_esperado")
  private BigDecimal valorEsperado;

  @JsonProperty("diferencia")
  private BigDecimal diferencia;

  @JsonProperty("estado")
  private String estado;

  @JsonProperty("archivo_id")
  private Long archivoId;

  @JsonProperty("fecha_creacion")
  private String fechaCreacion;

  // Constructor vacío
  public DescuentoProcesadoDTO() {}

  // Constructor completo
  public DescuentoProcesadoDTO(
      Long id,
      Integer clienteId,
      Integer seccionalId,
      Integer aseguradoraId,
      String cedulaCliente,
      String periodo,
      LocalDate fecha,
      String codigoDescuento,
      String codigoValidacion,
      BigDecimal valor,
      BigDecimal valorEsperado,
      BigDecimal diferencia,
      String estado,
      Long archivoId,
      LocalDateTime fechaCreacion) {
    this.id = id;
    this.clienteId = clienteId;
    this.seccionalId = seccionalId;
    this.aseguradoraId = aseguradoraId;
    this.cedulaCliente = cedulaCliente;
    this.periodo = periodo;
    this.fecha = fecha != null ? fecha.toString() : null;
    this.codigoDescuento = codigoDescuento;
    this.codigoValidacion = codigoValidacion;
    this.valor = valor;
    this.valorEsperado = valorEsperado;
    this.diferencia = diferencia;
    this.estado = estado;
    this.archivoId = archivoId;
    this.fechaCreacion =
        fechaCreacion != null
            ? fechaCreacion.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            : null;
  }

  // Getters y setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Integer getClienteId() {
    return clienteId;
  }

  public void setClienteId(Integer clienteId) {
    this.clienteId = clienteId;
  }

  public Integer getSeccionalId() {
    return seccionalId;
  }

  public void setSeccionalId(Integer seccionalId) {
    this.seccionalId = seccionalId;
  }

  public Integer getAseguradoraId() {
    return aseguradoraId;
  }

  public void setAseguradoraId(Integer aseguradoraId) {
    this.aseguradoraId = aseguradoraId;
  }

  public String getCedulaCliente() {
    return cedulaCliente;
  }

  public void setCedulaCliente(String cedulaCliente) {
    this.cedulaCliente = cedulaCliente;
  }

  public String getPeriodo() {
    return periodo;
  }

  public void setPeriodo(String periodo) {
    this.periodo = periodo;
  }

  public String getFecha() {
    return fecha;
  }

  public void setFecha(String fecha) {
    this.fecha = fecha;
  }

  public String getCodigoDescuento() {
    return codigoDescuento;
  }

  public void setCodigoDescuento(String codigoDescuento) {
    this.codigoDescuento = codigoDescuento;
  }

  public String getCodigoValidacion() {
    return codigoValidacion;
  }

  public void setCodigoValidacion(String codigoValidacion) {
    this.codigoValidacion = codigoValidacion;
  }

  public BigDecimal getValor() {
    return valor;
  }

  public void setValor(BigDecimal valor) {
    this.valor = valor;
  }

  public BigDecimal getValorEsperado() {
    return valorEsperado;
  }

  public void setValorEsperado(BigDecimal valorEsperado) {
    this.valorEsperado = valorEsperado;
  }

  public BigDecimal getDiferencia() {
    return diferencia;
  }

  public void setDiferencia(BigDecimal diferencia) {
    this.diferencia = diferencia;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public Long getArchivoId() {
    return archivoId;
  }

  public void setArchivoId(Long archivoId) {
    this.archivoId = archivoId;
  }

  public String getFechaCreacion() {
    return fechaCreacion;
  }

  public void setFechaCreacion(String fechaCreacion) {
    this.fechaCreacion = fechaCreacion;
  }
}
