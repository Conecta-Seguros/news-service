package conectaseguros.co.news_service.descuentos.infrastructure.input.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * DTO para representar un descuento procesado de un cliente
 */
public class DescuentoClienteDTO {

    @JsonProperty("cedula")
    private String cedula;

    @JsonProperty("periodo")
    private String periodo;

    @JsonProperty("codigo")
    private String codigo;

    @JsonProperty("valor_reportado")
    private BigDecimal valorReportado;

    @JsonProperty("valor_esperado")
    private BigDecimal valorEsperado;

    @JsonProperty("diferencia")
    private BigDecimal diferencia;

    @JsonProperty("estado")
    private String estado;

    // Constructor vacío
    public DescuentoClienteDTO() {}

    // Constructor completo
    public DescuentoClienteDTO(String cedula, String periodo, String codigo, BigDecimal valorReportado,
                              BigDecimal valorEsperado, BigDecimal diferencia, String estado) {
        this.cedula = cedula;
        this.periodo = periodo;
        this.codigo = codigo;
        this.valorReportado = valorReportado;
        this.valorEsperado = valorEsperado;
        this.diferencia = diferencia;
        this.estado = estado;
    }

    // Getters y Setters
    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public BigDecimal getValorReportado() {
        return valorReportado;
    }

    public void setValorReportado(BigDecimal valorReportado) {
        this.valorReportado = valorReportado;
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
}