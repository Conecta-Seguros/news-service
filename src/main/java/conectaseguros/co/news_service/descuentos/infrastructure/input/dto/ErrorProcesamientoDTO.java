package conectaseguros.co.news_service.descuentos.infrastructure.input.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import conectaseguros.co.news_service.descuentos.domain.model.ErrorProcesamiento;

import java.time.format.DateTimeFormatter;

public class ErrorProcesamientoDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("linea_archivo")
    private Integer lineaArchivo;

    @JsonProperty("cedula_cliente")
    private String cedulaCliente;

    @JsonProperty("tipo_error")
    private String tipoError;

    @JsonProperty("descripcion_error")
    private String descripcionError;

    @JsonProperty("contenido_linea")
    private String contenidoLinea;

    @JsonProperty("fecha_error")
    private String fechaError;

    @JsonProperty("es_critico")
    private Boolean esCritico;

    // Constructor vacío
    public ErrorProcesamientoDTO() {}

    // Constructor desde ErrorProcesamiento
    public ErrorProcesamientoDTO(ErrorProcesamiento error) {
        this.id = error.getId();
        this.lineaArchivo = error.getLineaArchivo();
        this.cedulaCliente = error.getCedulaCliente();
        this.tipoError = error.getTipoError().name();
        this.descripcionError = error.getDescripcionError();
        this.contenidoLinea = error.getContenidoLinea();
        this.fechaError = error.getFechaError().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.esCritico = error.getTipoError().esCritico();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getLineaArchivo() { return lineaArchivo; }
    public void setLineaArchivo(Integer lineaArchivo) { this.lineaArchivo = lineaArchivo; }

    public String getCedulaCliente() { return cedulaCliente; }
    public void setCedulaCliente(String cedulaCliente) { this.cedulaCliente = cedulaCliente; }

    public String getTipoError() { return tipoError; }
    public void setTipoError(String tipoError) { this.tipoError = tipoError; }

    public String getDescripcionError() { return descripcionError; }
    public void setDescripcionError(String descripcionError) { this.descripcionError = descripcionError; }

    public String getContenidoLinea() { return contenidoLinea; }
    public void setContenidoLinea(String contenidoLinea) { this.contenidoLinea = contenidoLinea; }

    public String getFechaError() { return fechaError; }
    public void setFechaError(String fechaError) { this.fechaError = fechaError; }

    public Boolean getEsCritico() { return esCritico; }
    public void setEsCritico(Boolean esCritico) { this.esCritico = esCritico; }
}