package conectaseguros.co.news_service.descuentos.application.service;

import conectaseguros.co.news_service.descuentos.domain.model.LineaDescuento;
import conectaseguros.co.news_service.descuentos.domain.model.ResultadoValidacion;
import conectaseguros.co.news_service.descuentos.domain.model.enums.TipoError;
import conectaseguros.co.news_service.descuentos.domain.port.input.ValidadorLinea;
import conectaseguros.co.news_service.descuentos.domain.port.output.ProductoService;
import conectaseguros.co.news_service.descuentos.infrastructure.output.product.ProductoInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ValidadorProductos implements ValidadorLinea {

    private static final Logger logger = LoggerFactory.getLogger(ValidadorProductos.class);

    private final ProductoService productoService;
    private ValidadorLinea siguiente;

    public ValidadorProductos(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Override
    public ResultadoValidacion validar(LineaDescuento linea, Integer seccionalId) {
        logger.debug("Validando productos para línea: {} - Cliente: {}",
                linea.getNumeroLinea(), linea.getCodigoEmpleado());

        // Usar directamente la cédula del empleado/cliente desde LineaDescuento
        String clienteCedula = linea.getCodigoEmpleado();

        if (clienteCedula == null || clienteCedula.trim().isEmpty()) {
            logger.warn("No se pudo obtener cédula de cliente para validar productos en línea: {}",
                    linea.getNumeroLinea());
            return ResultadoValidacion.conError(
                TipoError.ERROR_COMUNICACION_PRODUCTOS,
                "No se pudo obtener la cédula del cliente para la validación de productos"
            );
        }

        // Validar si el cliente tiene productos usando HEAD con la nueva implementación
        boolean tieneProductos = false;
        try {
            tieneProductos = productoService.clienteTieneProductosPorCedula(clienteCedula);
        } catch (Exception e) {
            logger.error("Error al validar existencia de productos para cliente cédula {}: {}", clienteCedula, e.getMessage());
            return ResultadoValidacion.conError(
                TipoError.ERROR_COMUNICACION_PRODUCTOS,
                "Error al validar existencia de productos: " + e.getMessage()
            );
        }

        if (!tieneProductos) {
            logger.debug("Cliente cédula {} no tiene productos asociados", clienteCedula);
            return ResultadoValidacion.conError(
                TipoError.SIN_PRODUCTOS_ACTIVOS,
                String.format("Cliente con cédula %s no tiene productos asociados", clienteCedula)
            );
        }

        // Validar productos activos usando el nuevo método
        var productosActivos = productoService.obtenerProductosActivosPorCedula(clienteCedula);
        if (productosActivos == null || productosActivos.isEmpty()) {
            logger.debug("Cliente cédula {} no tiene productos activos", clienteCedula);
            return ResultadoValidacion.conError(
                TipoError.SIN_PRODUCTOS_ACTIVOS,
                String.format("Cliente con cédula %s no tiene productos activos", clienteCedula)
            );
        }

        // Sumar el valor permitido por todos los productos activos (valorCuotaMensual)
        java.math.BigDecimal valorMaximoDescuento = productosActivos.stream()
            .map(p -> p.getValorCuotaMensual() != null ? p.getValorCuotaMensual() : java.math.BigDecimal.ZERO)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        // Validar que el valor solicitado en la línea no exceda el valor permitido
        if (linea.getValor().compareTo(valorMaximoDescuento) > 0) {
            logger.debug("Descuento solicitado ({}) excede el máximo permitido ({}) para cliente cédula {}",
                linea.getValor(), valorMaximoDescuento, clienteCedula);
            return ResultadoValidacion.conError(
                TipoError.ERROR_DESCUENTO_EXCEDE_MAXIMO,
                String.format("El descuento solicitado (%.2f) excede el máximo permitido (%.2f) por los productos activos del cliente con cédula %s",
                    linea.getValor(), valorMaximoDescuento, clienteCedula)
            );
        }

        logger.debug("Descuento solicitado ({}) es válido para cliente cédula {} (máximo permitido: {})",
            linea.getValor(), clienteCedula, valorMaximoDescuento);

        // Continuar con el siguiente validador
        if (siguiente != null) {
            return siguiente.validar(linea, seccionalId);
        }

        return ResultadoValidacion.exitoso();
    }

    @Override
    public ValidadorLinea setSiguiente(ValidadorLinea siguiente) {
        this.siguiente = siguiente;
        return this;
    }

    /**
     * Obtener productos activos por cédula (método actualizado)
     * @param clienteCedula Cédula del cliente
     * @return Lista de productos activos
     */
    public List<ProductoInfo> obtenerProductosActivos(String clienteCedula) {
        return productoService.obtenerProductosActivosPorCedula(clienteCedula);
    }

    /**
     * @deprecated Usar obtenerProductosActivos(String clienteCedula) en su lugar
     */
    @Deprecated
    public List<ProductoInfo> obtenerProductosActivos(Integer clienteId) {
        return productoService.obtenerProductosActivos(clienteId);
    }
}