package conectaseguros.co.news_service.descuentos.domain.port.output;

import conectaseguros.co.news_service.descuentos.domain.model.ResultadoValidacion;
import conectaseguros.co.news_service.descuentos.infrastructure.output.product.ProductoInfo;

import java.util.List;

public interface ProductoService {
    /**
     * Verifica si un cliente tiene productos activos
     * @param clienteId ID del cliente
     * @return Resultado de la validación con información de productos
     */
    ResultadoValidacion validarProductosActivos(Integer clienteId);

    /**
     * Obtiene la lista de productos activos de un cliente
     * @param clienteId ID del cliente
     * @return Lista de productos activos
     */
    List<ProductoInfo> obtenerProductosActivos(Integer clienteId);

    /**
     * Verifica si el cliente tiene productos asociados (HEAD)
     */
    boolean clienteTieneProductos(Integer clienteId);
}
