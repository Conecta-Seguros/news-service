package conectaseguros.co.news_service.descuentos.infrastructure.input.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClienteResponseDTO {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("cedula")
    private String cedula;

    @JsonProperty("nombre_completo")
    private String nombreCompleto;

    @JsonProperty("seccional_id")
    private Integer seccionalId;

    @JsonProperty("estado")
    private String estado;

    @JsonProperty("fecha_vinculacion")
    private String fechaVinculacion;

    @JsonProperty("activo")
    private Boolean activo;

    // Constructores
    public ClienteResponseDTO() {}

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public Integer getSeccionalId() { return seccionalId; }
    public void setSeccionalId(Integer seccionalId) { this.seccionalId = seccionalId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getFechaVinculacion() { return fechaVinculacion; }
    public void setFechaVinculacion(String fechaVinculacion) { this.fechaVinculacion = fechaVinculacion; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return String.format("ClienteResponseDTO{id=%d, cedula='%s', seccional=%d, activo=%s}",
                id, cedula, seccionalId, activo);
    }
}
