package conectaseguros.co.news_service.descuentos.infrastructure.input.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** DTO para respuesta paginada de descuentos procesados */
public class PaginatedDescuentoProcesadoDTO {

  @JsonProperty("data")
  private List<DescuentoProcesadoDTO> data;

  @JsonProperty("pagination")
  private PaginationDTO pagination;

  // Constructor vacío
  public PaginatedDescuentoProcesadoDTO() {}

  // Constructor completo
  public PaginatedDescuentoProcesadoDTO(
      List<DescuentoProcesadoDTO> data, PaginationDTO pagination) {
    this.data = data;
    this.pagination = pagination;
  }

  // Getters y setters
  public List<DescuentoProcesadoDTO> getData() {
    return data;
  }

  public void setData(List<DescuentoProcesadoDTO> data) {
    this.data = data;
  }

  public PaginationDTO getPagination() {
    return pagination;
  }

  public void setPagination(PaginationDTO pagination) {
    this.pagination = pagination;
  }
}
