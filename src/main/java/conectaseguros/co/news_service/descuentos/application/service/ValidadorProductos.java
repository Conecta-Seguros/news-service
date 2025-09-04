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

        // Obtener el id del cliente directamente del objeto LineaDescuento
        Integer clienteId = linea.getClienteId();

        if (clienteId == null) {
            logger.warn("No se pudo obtener ID de cliente para validar productos: {}",
                    linea.getCodigoEmpleado());
            return ResultadoValidacion.conError(
                TipoError.ERROR_COMUNICACION_PRODUCTOS,
                "No se pudo obtener el ID del cliente para la validación de productos"
            );
        }

        // Validar si el cliente tiene productos usando HEAD
        boolean tieneProductos = false;
        try {
            tieneProductos = productoService.clienteTieneProductos(clienteId);
        } catch (Exception e) {
            logger.error("Error al validar existencia de productos para cliente {}: {}", clienteId, e.getMessage());
            return ResultadoValidacion.conError(
                TipoError.ERROR_COMUNICACION_PRODUCTOS,
                "Error al validar existencia de productos: " + e.getMessage()
            );
        }

        if (!tieneProductos) {
            logger.debug("Cliente {} no tiene productos asociados", clienteId);
            return ResultadoValidacion.conError(
                TipoError.SIN_PRODUCTOS_ACTIVOS,
                String.format("Cliente %d no tiene productos asociados", clienteId)
            );
        }

        // Validar productos activos
        var productosActivos = productoService.obtenerProductosActivos(clienteId);
        if (productosActivos == null || productosActivos.isEmpty()) {
            logger.debug("Cliente {} no tiene productos activos", clienteId);
            return ResultadoValidacion.conError(
                TipoError.SIN_PRODUCTOS_ACTIVOS,
                String.format("Cliente %d no tiene productos activos", clienteId)
            );
        }

        // Sumar el valor permitido por todos los productos activos (valorCuotaMensual)
        java.math.BigDecimal valorMaximoDescuento = productosActivos.stream()
            .map(p -> p.getValorCuotaMensual() != null ? p.getValorCuotaMensual() : java.math.BigDecimal.ZERO)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        // Validar que el valor solicitado en la línea no exceda el valor permitido
        if (linea.getValor().compareTo(valorMaximoDescuento) > 0) {
            logger.debug("Descuento solicitado ({}) excede el máximo permitido ({}) para cliente {}", linea.getValor(), valorMaximoDescuento, clienteId);
            return ResultadoValidacion.conError(
                TipoError.ERROR_DESCUENTO_EXCEDE_MAXIMO,
                String.format("El descuento solicitado (%.2f) excede el máximo permitido (%.2f) por los productos activos del cliente %d", linea.getValor(), valorMaximoDescuento, clienteId)
            );
        }

        logger.debug("Descuento solicitado ({}) es válido para cliente {} (máximo permitido: {})", linea.getValor(), clienteId, valorMaximoDescuento);

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

    public List<ProductoInfo> obtenerProductosActivos(Integer clienteId) {
        return productoService.obtenerProductosActivos(clienteId);
    }
}