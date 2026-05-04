package conectaseguros.co.news_service.descuentos.domain.port.output;

import conectaseguros.co.news_service.descuentos.domain.model.ResultadoValidacion;
import conectaseguros.co.news_service.descuentos.infrastructure.output.product.ProductoInfo;
import java.util.List;

public interface ProductoService {
  /**
   * Verifica si un cliente tiene productos activos
   *
   * @param clienteId ID del cliente
   * @return Resultado de la validación con información de productos
   * @deprecated Usar validarProductosActivosPorCedula en su lugar
   */
  @Deprecated
  ResultadoValidacion validarProductosActivos(Integer clienteId);

  /**
   * Verifica si un cliente tiene productos activos por cédula
   *
   * @param clienteCedula Cédula del cliente
   * @return Resultado de la validación con información de productos
   */
  ResultadoValidacion validarProductosActivosPorCedula(String clienteCedula);

  /**
   * Obtiene la lista de productos activos de un cliente
   *
   * @param clienteId ID del cliente
   * @return Lista de productos activos
   * @deprecated Usar obtenerProductosActivosPorCedula en su lugar
   */
  @Deprecated
  List<ProductoInfo> obtenerProductosActivos(Integer clienteId);

  /**
   * Obtiene la lista de productos activos de un cliente por cédula
   *
   * @param clienteCedula Cédula del cliente
   * @return Lista de productos activos
   */
  List<ProductoInfo> obtenerProductosActivosPorCedula(String clienteCedula);

  /**
   * Verifica si el cliente tiene productos asociados (HEAD)
   *
   * @deprecated Usar clienteTieneProductosPorCedula en su lugar
   */
  @Deprecated
  boolean clienteTieneProductos(Integer clienteId);

  /**
   * Verifica si el cliente tiene productos asociados por cédula (HEAD)
   *
   * @param clienteCedula Cédula del cliente
   * @return true si el cliente tiene productos, false en caso contrario
   */
  boolean clienteTieneProductosPorCedula(String clienteCedula);

  /**
   * Limpia el cache interno de productos. Debe llamarse al inicio de cada procesamiento para
   * garantizar que se consulten datos actualizados del products-service.
   *
   * <p>Las implementaciones con cache deben sobreescribir este método; la implementación por
   * defecto es un no-op.
   */
  default void limpiarCache() {}
}
