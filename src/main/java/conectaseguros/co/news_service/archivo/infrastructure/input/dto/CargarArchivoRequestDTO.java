package conectaseguros.co.news_service.archivo.infrastructure.input.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public class CargarArchivoRequestDTO {

    @NotBlank(message = "El tipo de archivo es obligatorio")
    @Pattern(regexp = "NOVEDADES|DESCUENTOS", message = "Tipo de archivo debe ser NOVEDADES o DESCUENTOS")
    private String tipoArchivo;

    @NotBlank(message = "El período es obligatorio")
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "El período debe tener formato YYYY-MM")
    private String periodo;

    @NotNull(message = "El ID de seccional es obligatorio")
    @Positive(message = "El ID de seccional debe ser un número positivo")
    private Integer seccionalId;

    public CargarArchivoRequestDTO() {}

    public CargarArchivoRequestDTO(String tipoArchivo, String periodo, Integer seccionalId) {
        this.tipoArchivo = tipoArchivo;
        this.periodo = periodo;
        this.seccionalId = seccionalId;
    }

    // Getters y Setters
    public String getTipoArchivo() { return tipoArchivo; }
    public void setTipoArchivo(String tipoArchivo) { this.tipoArchivo = tipoArchivo; }
    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }
    public Integer getSeccionalId() { return seccionalId; }
    public void setSeccionalId(Integer seccionalId) { this.seccionalId = seccionalId; }
}
