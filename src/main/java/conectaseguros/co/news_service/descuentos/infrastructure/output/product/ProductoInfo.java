package conectaseguros.co.news_service.descuentos.infrastructure.output.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO que representa la información de un producto activo de un cliente
 * obtenida desde el microservicio de productos.
 */
public class ProductoInfo {
    private final Integer id;
    private final Integer polizaId;
    private final String aseguradoCedula;
    private final String estado;
    private final LocalDateTime fechaCreacion;
    private final LocalDateTime fechaCancelacion;
    private final String motivoCancelacion;
    private final String usuarioCreacion;
    private final String usuarioModificacion;
    private final BigDecimal valorTotalAnual;
    private final BigDecimal cuotaMensual;

    // Constructor principal para todos los campos
    public ProductoInfo(Integer id, Integer polizaId, String aseguradoCedula, String estado,
                       LocalDateTime fechaCreacion, LocalDateTime fechaCancelacion, String motivoCancelacion,
                       String usuarioCreacion, String usuarioModificacion,
                       BigDecimal valorTotalAnual, BigDecimal cuotaMensual) {
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
    }

    // Constructor de compatibilidad con la versión anterior (deprecated)
    @Deprecated
    public ProductoInfo(Integer id, String codigo, String nombre, Boolean vigente,
                       BigDecimal valorAnual, BigDecimal valorCuotaMensual) {
        this.id = id;
        this.polizaId = null;
        this.aseguradoCedula = codigo; // Mapeo de compatibilidad
        this.estado = vigente != null && vigente ? "ACTIVO" : "INACTIVO";
        this.fechaCreacion = null;
        this.fechaCancelacion = null;
        this.motivoCancelacion = null;
        this.usuarioCreacion = null;
        this.usuarioModificacion = null;
        this.valorTotalAnual = valorAnual;
        this.cuotaMensual = valorCuotaMensual;
    }

    // Getters
    public Integer getId() { return id; }
    public Integer getPolizaId() { return polizaId; }
    public String getAseguradoCedula() { return aseguradoCedula; }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaCancelacion() { return fechaCancelacion; }
    public String getMotivoCancelacion() { return motivoCancelacion; }
    public String getUsuarioCreacion() { return usuarioCreacion; }
    public String getUsuarioModificacion() { return usuarioModificacion; }
    public BigDecimal getValorTotalAnual() { return valorTotalAnual; }
    public BigDecimal getCuotaMensual() { return cuotaMensual; }

    // Getters de compatibilidad con la versión anterior (deprecated)
    @Deprecated
    public String getCodigo() { return aseguradoCedula; }

    @Deprecated
    public String getNombre() { return "Producto ID: " + id; }

    @Deprecated
    public Boolean getVigente() { return "ACTIVO".equals(estado); }

    @Deprecated
    public BigDecimal getValorAnual() { return valorTotalAnual; }

    @Deprecated
    public BigDecimal getValorCuotaMensual() { return cuotaMensual; }

    /**
     * Verifica si el producto está activo
     */
    public boolean isActivo() {
        return "ACTIVO".equals(estado);
    }

    /**
     * Verifica si el producto tiene cuota mensual válida para descuentos
     */
    public boolean tieneCuotaMensualValida() {
        return cuotaMensual != null && cuotaMensual.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public String toString() {
        return "ProductoInfo{" +
                "id=" + id +
                ", polizaId=" + polizaId +
                ", aseguradoCedula='" + aseguradoCedula + '\'' +
                ", estado='" + estado + '\'' +
                ", valorTotalAnual=" + valorTotalAnual +
                ", cuotaMensual=" + cuotaMensual +
                '}';
    }
}
