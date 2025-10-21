package conectaseguros.co.news_service.archivo.infrastructure.input.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ArchivoResponseDTO {
  // Getters y Setters
  private Long id;
  private String nombreArchivo;
  private String periodo;
  private Integer seccionalId;
  private Integer totalRegistros;
  private Integer registrosExitosos;
  private Integer registrosErrores;
  private Integer excedentes;
  private Integer faltantes;
  private Integer noLlegaron;
  private String estado;
  private String tipoArchivo;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime fechaCarga;

  private Integer usuarioCarga;
  private Long tamanoBytes;

  public ArchivoResponseDTO() {}

  // Builder pattern
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final ArchivoResponseDTO dto = new ArchivoResponseDTO();

    public Builder id(Long id) {
      dto.id = id;
      return this;
    }

    public Builder nombreArchivo(String nombreArchivo) {
      dto.nombreArchivo = nombreArchivo;
      return this;
    }

    public Builder periodo(String periodo) {
      dto.periodo = periodo;
      return this;
    }

    public Builder seccionalId(Integer seccionalId) {
      dto.seccionalId = seccionalId;
      return this;
    }

    public Builder totalRegistros(Integer totalRegistros) {
      dto.totalRegistros = totalRegistros;
      return this;
    }

    public Builder registrosExitosos(Integer registrosExitosos) {
      dto.registrosExitosos = registrosExitosos;
      return this;
    }

    public Builder registrosErrores(Integer registrosErrores) {
      dto.registrosErrores = registrosErrores;
      return this;
    }

    public Builder excedentes(Integer excedentes) {
      dto.excedentes = excedentes;
      return this;
    }

    public Builder faltantes(Integer faltantes) {
      dto.faltantes = faltantes;
      return this;
    }

    public Builder noLlegaron(Integer noLlegaron) {
      dto.noLlegaron = noLlegaron;
      return this;
    }

    public Builder estado(String estado) {
      dto.estado = estado;
      return this;
    }

    public Builder tipoArchivo(String tipoArchivo) {
      dto.tipoArchivo = tipoArchivo;
      return this;
    }

    public Builder fechaCarga(LocalDateTime fechaCarga) {
      dto.fechaCarga = fechaCarga;
      return this;
    }

    public Builder usuarioCarga(Integer usuarioCarga) {
      dto.usuarioCarga = usuarioCarga;
      return this;
    }

    public Builder tamanoBytes(Long tamanoBytes) {
      dto.tamanoBytes = tamanoBytes;
      return this;
    }

    public ArchivoResponseDTO build() {
      return dto;
    }
  }
}
