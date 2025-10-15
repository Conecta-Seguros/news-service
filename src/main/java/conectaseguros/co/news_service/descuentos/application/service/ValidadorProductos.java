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

import java.math.BigDecimal;

@Component
public class ValidadorProductos implements ValidadorLinea {

    private static final Logger logger = LoggerFactory.getLogger(ValidadorProductos.class);

    private final ProductoService productoService;
    private ValidadorLinea siguiente;

    public ValidadorProductos(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Override
    public ResultadoValidacion validar(LineaDescuento linea, Integer seccionalId, String periodo) {
        logger.debug("Validando productos para línea: {} - Cliente: {} - Período: {}",
                linea.getNumeroLinea(), linea.getCodigoEmpleado(), periodo);

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

        if (periodo == null || periodo.trim().isEmpty()) {
            logger.warn("No se pudo obtener período para validar productos en línea: {}",
                    linea.getNumeroLinea());
            return ResultadoValidacion.conError(
                TipoError.ERROR_COMUNICACION_PRODUCTOS,
                "No se pudo obtener el período para la validación de productos"
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

        // ✅ CORRECCIÓN: Usar el período específico para calcular el valor máximo permitido
        BigDecimal valorMaximoDescuento = BigDecimal.ZERO;
        boolean tieneProductoConPagoValidoParaPeriodo = false;

        for (ProductoInfo producto : productosActivos) {
            BigDecimal valorParaPeriodo = producto.getValorParaPeriodo(periodo);

            if (valorParaPeriodo.compareTo(BigDecimal.ZERO) > 0) {
                valorMaximoDescuento = valorMaximoDescuento.add(valorParaPeriodo);
                tieneProductoConPagoValidoParaPeriodo = true;

                logger.debug("Producto ID: {} tiene valor {} para período {}",
                    producto.getId(), valorParaPeriodo, periodo);
            } else {
                logger.debug("Producto ID: {} no tiene valor válido para período {} (períodos disponibles: {})",
                    producto.getId(), periodo, producto.getPeriodosDisponibles());
            }
        }

        if (!tieneProductoConPagoValidoParaPeriodo) {
            logger.debug("Cliente cédula {} no tiene productos con pagos válidos para el período {}",
                clienteCedula, periodo);
            return ResultadoValidacion.conError(
                TipoError.SIN_PRODUCTOS_ACTIVOS,
                String.format("Cliente con cédula %s no tiene productos con pagos válidos para el período %s",
                    clienteCedula, periodo)
            );
        }

        // ✅ CORRECCIÓN: NO validar si excede el máximo - permitir descuentos EXCEDENTES
        // Los descuentos que excedan el valor esperado se marcarán como "EXCEDENTE" en el procesamiento
        logger.debug("Descuento solicitado ({}) para cliente cédula {} en período {} (valor esperado: {})",
            linea.getValor(), clienteCedula, periodo, valorMaximoDescuento);

        if (linea.getValor().compareTo(valorMaximoDescuento) > 0) {
            logger.debug("Descuento solicitado ({}) excede el valor esperado ({}) - se guardará como EXCEDENTE",
                linea.getValor(), valorMaximoDescuento);
        } else if (linea.getValor().compareTo(valorMaximoDescuento) < 0) {
            logger.debug("Descuento solicitado ({}) es menor al valor esperado ({}) - se guardará como FALTANTE",
                linea.getValor(), valorMaximoDescuento);
        } else {
            logger.debug("Descuento solicitado ({}) coincide con el valor esperado ({}) - se guardará como CONCILIADO",
                linea.getValor(), valorMaximoDescuento);
        }

        // ✅ IMPORTANTE: Almacenar el valor esperado en el contexto de validación
        ResultadoValidacion resultado = ResultadoValidacion.exitoso();
        resultado.setValorEsperado(valorMaximoDescuento);

        // Continuar con el siguiente validador
        if (siguiente != null) {
            return siguiente.validar(linea, seccionalId, periodo);
        }

        return resultado;
    }

    @Override
    public ValidadorLinea setSiguiente(ValidadorLinea siguiente) {
        this.siguiente = siguiente;
        return this;
    }

    /**
     * Getter para acceder al ProductoService desde el servicio de procesamiento
     * @return La instancia de ProductoService
     */
    public ProductoService getProductoService() {
        return this.productoService;
    }
}