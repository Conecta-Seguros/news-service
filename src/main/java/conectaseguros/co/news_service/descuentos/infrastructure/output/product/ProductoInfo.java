package conectaseguros.co.news_service.descuentos.infrastructure.output.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DTO que representa la información de un producto activo de un cliente obtenida desde el
 * microservicio de productos.
 */
public class ProductoInfo {
  private static final Logger logger = LoggerFactory.getLogger(ProductoInfo.class);

  private final Integer id;
  private final Integer polizaId;
  private final String aseguradoCedula;
  private final String estado;
  private final LocalDateTime fechaCreacion;
  private final LocalDateTime fechaCancelacion;
  private final String motivoCancelacion;
  private final String usuarioCreacion;
  private final String usuarioModificacion;
  private final BigDecimal valorTotalPeriodo;
  private final Map<String, BigDecimal> planDePagos;
  // Campos de compatibilidad con versión anterior
  @Deprecated private final BigDecimal valorTotalAnual;
  @Deprecated private final BigDecimal cuotaMensual;

  // Constructor principal para la nueva estructura
  public ProductoInfo(
      Integer id,
      Integer polizaId,
      String aseguradoCedula,
      String estado,
      LocalDateTime fechaCreacion,
      LocalDateTime fechaCancelacion,
      String motivoCancelacion,
      String usuarioCreacion,
      String usuarioModificacion,
      BigDecimal valorTotalPeriodo,
      Map<String, BigDecimal> planDePagos) {
    this.id = id;
    this.polizaId = polizaId;
    this.aseguradoCedula = aseguradoCedula;
    this.estado = estado;
    this.fechaCreacion = fechaCreacion;
    this.fechaCancelacion = fechaCancelacion;
    this.motivoCancelacion = motivoCancelacion;
    this.usuarioCreacion = usuarioCreacion;
    this.usuarioModificacion = usuarioModificacion;
    this.valorTotalPeriodo = valorTotalPeriodo;
    this.planDePagos = planDePagos;
    // Valores por compatibilidad
    this.valorTotalAnual = valorTotalPeriodo;
    this.cuotaMensual =
        planDePagos != null && !planDePagos.isEmpty()
            ? planDePagos.values().iterator().next()
            : BigDecimal.ZERO;
  }

  // Constructor de compatibilidad con la versión anterior (deprecated)
  @Deprecated
  public ProductoInfo(
      Integer id,
      Integer polizaId,
      String aseguradoCedula,
      String estado,
      LocalDateTime fechaCreacion,
      LocalDateTime fechaCancelacion,
      String motivoCancelacion,
      String usuarioCreacion,
      String usuarioModificacion,
      BigDecimal valorTotalAnual,
      BigDecimal cuotaMensual) {
    this.id = id;
    this.polizaId = polizaId;
    this.aseguradoCedula = aseguradoCedula;
    this.estado = estado;
    this.fechaCreacion = fechaCreacion;
    this.fechaCancelacion = fechaCancelacion;
    this.motivoCancelacion = motivoCancelacion;
    this.usuarioCreacion = usuarioCreacion;
    this.usuarioModificacion = usuarioModificacion;
    this.valorTotalAnual = valorTotalAnual;
    this.cuotaMensual = cuotaMensual;
    // Nuevos valores con fallback
    this.valorTotalPeriodo = valorTotalAnual;
    this.planDePagos = cuotaMensual != null ? Map.of("default", cuotaMensual) : Map.of();
  }

  // Constructor legacy (deprecated)
  @Deprecated
  public ProductoInfo(
      Integer id,
      String codigo,
      String nombre,
      Boolean vigente,
      BigDecimal valorAnual,
      BigDecimal valorCuotaMensual) {
    this.id = id;
    this.polizaId = null;
    this.aseguradoCedula = codigo;
    this.estado = vigente != null && vigente ? "ACTIVO" : "INACTIVO";
    this.fechaCreacion = null;
    this.fechaCancelacion = null;
    this.motivoCancelacion = null;
    this.usuarioCreacion = null;
    this.usuarioModificacion = null;
    this.valorTotalAnual = valorAnual;
    this.cuotaMensual = valorCuotaMensual;
    // Nuevos valores con fallback
    this.valorTotalPeriodo = valorAnual;
    this.planDePagos = valorCuotaMensual != null ? Map.of("default", valorCuotaMensual) : Map.of();
  }

  // Getters principales
  public Integer getId() {
    return id;
  }

  public Integer getPolizaId() {
    return polizaId;
  }

  public String getAseguradoCedula() {
    return aseguradoCedula;
  }

  public String getEstado() {
    return estado;
  }

  public LocalDateTime getFechaCreacion() {
    return fechaCreacion;
  }

  public LocalDateTime getFechaCancelacion() {
    return fechaCancelacion;
  }

  public String getMotivoCancelacion() {
    return motivoCancelacion;
  }

  public String getUsuarioCreacion() {
    return usuarioCreacion;
  }

  public String getUsuarioModificacion() {
    return usuarioModificacion;
  }

  public BigDecimal getValorTotalPeriodo() {
    return valorTotalPeriodo;
  }

  public Map<String, BigDecimal> getPlanDePagos() {
    return planDePagos;
  }

  // Getters de compatibilidad (deprecated)
  @Deprecated
  public BigDecimal getValorTotalAnual() {
    return valorTotalAnual != null ? valorTotalAnual : valorTotalPeriodo;
  }

  @Deprecated
  public BigDecimal getCuotaMensual() {
    return cuotaMensual != null
        ? cuotaMensual
        : (planDePagos != null && !planDePagos.isEmpty()
            ? planDePagos.values().iterator().next()
            : BigDecimal.ZERO);
  }

  @Deprecated
  public String getCodigo() {
    return aseguradoCedula;
  }

  @Deprecated
  public String getNombre() {
    return "Producto ID: " + id;
  }

  @Deprecated
  public Boolean getVigente() {
    return "ACTIVO".equals(estado);
  }

  @Deprecated
  public BigDecimal getValorAnual() {
    return getValorTotalAnual();
  }

  @Deprecated
  public BigDecimal getValorCuotaMensual() {
    return getCuotaMensual();
  }

  /** Verifica si el producto está activo */
  public boolean isActivo() {
    return "ACTIVO".equals(estado);
  }

  /**
   * Verifica si el producto tiene cuota mensual válida para descuentos
   *
   * @deprecated Usar tienePagoValidoParaPeriodo(String periodo)
   */
  @Deprecated
  public boolean tieneCuotaMensualValida() {
    return getCuotaMensual() != null && getCuotaMensual().compareTo(BigDecimal.ZERO) > 0;
  }

  /**
   * Obtiene el valor del pago para un período específico
   *
   * @param periodo El período en formato YYYY-MM (ej: "2025-10")
   * @return El valor del pago para ese período o BigDecimal.ZERO si no existe
   */
  public BigDecimal getValorParaPeriodo(String periodo) {
    if (planDePagos == null || periodo == null) {
      logger.debug("Plan de pagos es null o período es null para producto ID: {}", id);
      return BigDecimal.ZERO;
    }

    logger.debug(
        "Buscando período '{}' en plan de pagos del producto ID: {}. Períodos disponibles: {}",
        periodo,
        id,
        planDePagos.keySet());

    BigDecimal valor = planDePagos.getOrDefault(periodo, BigDecimal.ZERO);

    if (valor.compareTo(BigDecimal.ZERO) == 0) {
      logger.warn(
          "No se encontró valor para período '{}' en producto ID: {}. Plan de pagos: {}",
          periodo,
          id,
          planDePagos);
    } else {
      logger.debug(
          "Valor encontrado para período '{}' en producto ID: {} = {}", periodo, id, valor);
    }

    return valor;
  }

  /**
   * Verifica si el producto tiene un pago válido para el período especificado
   *
   * @param periodo El período en formato YYYY-MM (ej: "2025-10")
   * @return true si existe un pago válido para ese período
   */
  public boolean tienePagoValidoParaPeriodo(String periodo) {
    BigDecimal valor = getValorParaPeriodo(periodo);
    return valor != null && valor.compareTo(BigDecimal.ZERO) > 0;
  }

  /**
   * Obtiene todos los períodos disponibles en el plan de pagos
   *
   * @return Set con todos los períodos disponibles
   */
  public java.util.Set<String> getPeriodosDisponibles() {
    return planDePagos != null ? planDePagos.keySet() : java.util.Set.of();
  }

  @Override
  public String toString() {
    return "ProductoInfo{"
        + "id="
        + id
        + ", polizaId="
        + polizaId
        + ", aseguradoCedula='"
        + aseguradoCedula
        + '\''
        + ", estado='"
        + estado
        + '\''
        + ", valorTotalPeriodo="
        + valorTotalPeriodo
        + ", planDePagos="
        + planDePagos
        + '}';
  }
}
