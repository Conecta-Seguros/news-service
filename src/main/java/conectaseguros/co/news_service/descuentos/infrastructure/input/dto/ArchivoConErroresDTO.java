package conectaseguros.co.news_service.descuentos.infrastructure.input.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para representar un archivo con errores
 */
public class ArchivoConErroresDTO {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("nombre")
    private String nombre;
    
    @JsonProperty("periodo")
    private String periodo;
    
    @JsonProperty("seccional_id")
    private Integer seccionalId;
    
    @JsonProperty("total_errores")
    private long totalErrores;
    
    @JsonProperty("estado")
    private String estado;
    
    // Constructor vacío
    public ArchivoConErroresDTO() {}
    
    // Constructor sin estado
    public ArchivoConErroresDTO(Long id, String nombre, String periodo, Integer seccionalId, long totalErrores) {
        this.id = id;
        this.nombre = nombre;
        this.periodo = periodo;
        this.seccionalId = seccionalId;
        this.totalErrores = totalErrores;
    }
    
    // Constructor completo
    public ArchivoConErroresDTO(Long id, String nombre, String periodo, Integer seccionalId, long totalErrores, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.periodo = periodo;
        this.seccionalId = seccionalId;
        this.totalErrores = totalErrores;
        this.estado = estado;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getPeriodo() {
        return periodo;
    }
    
    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }
    
    public Integer getSeccionalId() {
        return seccionalId;
    }
    
    public void setSeccionalId(Integer seccionalId) {
        this.seccionalId = seccionalId;
    }
    
    public long getTotalErrores() {
        return totalErrores;
    }
    
    public void setTotalErrores(long totalErrores) {
        this.totalErrores = totalErrores;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
}