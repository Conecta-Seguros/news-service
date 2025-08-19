package conectaseguros.co.news_service.descuentos.domain.port.output;

import conectaseguros.co.news_service.descuentos.domain.model.ResultadoValidacion;
import lombok.Getter;

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
     * Información básica del producto
     */
    @Getter
    class ProductoInfo {
        // Getters
        private final Integer id;
        private final String codigo;
        private final String nombre;
        private final boolean vigente;

        public ProductoInfo(Integer id, String codigo, String nombre, boolean vigente) {
            this.id = id;
            this.codigo = codigo;
            this.nombre = nombre;
            this.vigente = vigente;
        }

        @Override
        public String toString() {
            return String.format("ProductoInfo{id=%d, codigo='%s', vigente=%s}",
                    id, codigo, vigente);
        }
    }
}
