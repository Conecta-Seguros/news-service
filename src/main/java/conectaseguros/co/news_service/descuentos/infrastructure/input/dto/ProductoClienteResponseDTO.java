package conectaseguros.co.news_service.descuentos.infrastructure.input.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class ProductoClienteResponseDTO {

  @JsonProperty("cliente_id")
  private Integer clienteId;

  @JsonProperty("productos")
  private List<ProductoDTO> productos;

  @JsonProperty("total_productos_activos")
  private Integer totalProductosActivos;

  // Constructor
  public ProductoClienteResponseDTO() {}

  // Getters y Setters
  public Integer getClienteId() {
    return clienteId;
  }

  public void setClienteId(Integer clienteId) {
    this.clienteId = clienteId;
  }

  public List<ProductoDTO> getProductos() {
    return productos;
  }

  public void setProductos(List<ProductoDTO> productos) {
    this.productos = productos;
  }

  public Integer getTotalProductosActivos() {
    return totalProductosActivos;
  }

  public void setTotalProductosActivos(Integer totalProductosActivos) {
    this.totalProductosActivos = totalProductosActivos;
  }

  @Setter
  @Getter
  public static class ProductoDTO {
    // Getters y Setters
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("codigo")
    private String codigo;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("estado")
    private String estado;

    @JsonProperty("vigente")
    private Boolean vigente;
  }
}
