package conectaseguros.co.news_service.descuentos.infrastructure.input.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import conectaseguros.co.news_service.descuentos.domain.model.EstadoProcesamiento;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProcesamientoResponseDTO {

    @JsonProperty("archivo_id")
    private Long archivoId;

    @JsonProperty("estado")
    private String estado;

    @JsonProperty("descripcion_estado")
    private String descripcionEstado;

    @JsonProperty("total_lineas")
    private Integer totalLineas;

    @JsonProperty("lineas_procesadas")
    private Integer lineasProcesadas;

    @JsonProperty("lineas_exitosas")
    private Integer lineasExitosas;

    @JsonProperty("lineas_con_error")
    private Integer lineasConError;

    @JsonProperty("excedentes")
    private Integer excedentes;

    @JsonProperty("faltantes")
    private Integer faltantes;

    @JsonProperty("no_llegaron")
    private Integer noLlegaron;

    @JsonProperty("porcentaje_progreso")
    private Double porcentajeProgreso;

    @JsonProperty("fecha_inicio")
    private String fechaInicio;

    @JsonProperty("fecha_fin")
    private String fechaFin;

    @JsonProperty("mensaje_estado")
    private String mensajeEstado;

    @JsonProperty("esta_completo")
    private Boolean estaCompleto;

    @JsonProperty("tiene_errores")
    private Boolean tieneErrores;

    // Constructor vacío
    public ProcesamientoResponseDTO() {}

    // Constructor desde EstadoProcesamiento
    public ProcesamientoResponseDTO(Long archivoId, EstadoProcesamiento estado) {
        this.archivoId = archivoId;
        this.estado = estado.getEstado().getValor();
        this.descripcionEstado = estado.getEstado().getDescripcion();
        this.totalLineas = estado.getTotalLineas();
        this.lineasProcesadas = estado.getLineasProcesadas();
        this.lineasExitosas = estado.getLineasExitosas();
        this.lineasConError = estado.getLineasConError();
        this.excedentes = estado.getExcedentes();
        this.faltantes = estado.getFaltantes();
        this.noLlegaron = estado.getNoLlegaron();
        this.porcentajeProgreso = estado.getPorcentajeProgreso();
        this.fechaInicio = formatearFecha(estado.getFechaInicio());
        this.fechaFin = formatearFecha(estado.getFechaFin());
        this.mensajeEstado = estado.getMensajeEstado();
        this.estaCompleto = estado.estaCompleto();
        this.tieneErrores = estado.tieneErrores();
    }

    private String formatearFecha(LocalDateTime fecha) {
        if (fecha == null) return null;
        return fecha.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Getters y Setters
    public Long getArchivoId() { return archivoId; }
    public void setArchivoId(Long archivoId) { this.archivoId = archivoId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getDescripcionEstado() { return descripcionEstado; }
    public void setDescripcionEstado(String descripcionEstado) { this.descripcionEstado = descripcionEstado; }

    public Integer getTotalLineas() { return totalLineas; }
    public void setTotalLineas(Integer totalLineas) { this.totalLineas = totalLineas; }

    public Integer getLineasProcesadas() { return lineasProcesadas; }
    public void setLineasProcesadas(Integer lineasProcesadas) { this.lineasProcesadas = lineasProcesadas; }

    public Integer getLineasExitosas() { return lineasExitosas; }
    public void setLineasExitosas(Integer lineasExitosas) { this.lineasExitosas = lineasExitosas; }

    public Integer getLineasConError() { return lineasConError; }
    public void setLineasConError(Integer lineasConError) { this.lineasConError = lineasConError; }

    public Integer getExcedentes() { return excedentes; }
    public void setExcedentes(Integer excedentes) { this.excedentes = excedentes; }

    public Integer getFaltantes() { return faltantes; }
    public void setFaltantes(Integer faltantes) { this.faltantes = faltantes; }

    public Integer getNoLlegaron() { return noLlegaron; }
    public void setNoLlegaron(Integer noLlegaron) { this.noLlegaron = noLlegaron; }

    public Double getPorcentajeProgreso() { return porcentajeProgreso; }
    public void setPorcentajeProgreso(Double porcentajeProgreso) { this.porcentajeProgreso = porcentajeProgreso; }

    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }

    public String getFechaFin() { return fechaFin; }
    public void setFechaFin(String fechaFin) { this.fechaFin = fechaFin; }

    public String getMensajeEstado() { return mensajeEstado; }
    public void setMensajeEstado(String mensajeEstado) { this.mensajeEstado = mensajeEstado; }

    public Boolean getEstaCompleto() { return estaCompleto; }
    public void setEstaCompleto(Boolean estaCompleto) { this.estaCompleto = estaCompleto; }

    public Boolean getTieneErrores() { return tieneErrores; }
    public void setTieneErrores(Boolean tieneErrores) { this.tieneErrores = tieneErrores; }
}
