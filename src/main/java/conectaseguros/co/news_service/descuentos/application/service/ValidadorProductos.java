package conectaseguros.co.news_service.descuentos.application.service;

import conectaseguros.co.news_service.descuentos.domain.model.LineaDescuento;
import conectaseguros.co.news_service.descuentos.domain.model.ResultadoValidacion;
import conectaseguros.co.news_service.descuentos.domain.port.input.ValidadorLinea;
import conectaseguros.co.news_service.descuentos.domain.port.output.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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

        // Necesitamos el ID del cliente que debería venir del validador anterior
        // Por simplicidad, vamos a hacer otra consulta al servicio de clientes
        // En una implementación más sofisticada, pasaríamos el contexto entre validadores

        try {
            // Aquí asumiríamos que tenemos el ID del cliente desde el contexto anterior
            // Por ahora, simulamos obtenerlo (en producción se optimizaría esto)
            Integer clienteId = obtenerClienteId(linea.getCodigoEmpleado());

            if (clienteId == null) {
                // Este error ya debería haber sido capturado en ValidadorCliente
                logger.warn("No se pudo obtener ID de cliente para validar productos: {}",
                        linea.getCodigoEmpleado());
                return ResultadoValidacion.exitoso(); // Continuar sin validar productos
            }

            ResultadoValidacion validacionProductos = productoService.validarProductosActivos(clienteId);

            if (validacionProductos.tieneErrores()) {
                logger.debug("Cliente {} no tiene productos activos: {}",
                        linea.getCodigoEmpleado(), validacionProductos.getDescripcionErrores());
                return validacionProductos;
            }

            logger.debug("Cliente {} tiene productos activos válidos", linea.getCodigoEmpleado());

            // Continuar con el siguiente validador
            if (siguiente != null) {
                return siguiente.validar(linea, seccionalId);
            }

            return ResultadoValidacion.exitoso();

        } catch (Exception e) {
            logger.error("Error inesperado al validar productos para cliente {}",
                    linea.getCodigoEmpleado(), e);
            return ResultadoValidacion.exitoso(); // No bloquear por errores de productos
        }
    }

    @Override
    public ValidadorLinea setSiguiente(ValidadorLinea siguiente) {
        this.siguiente = siguiente;
        return this;
    }

    private Integer obtenerClienteId(String cedula) {
        // En una implementación real, esto vendría del contexto de validación
        // o se haría un cache de las consultas ya realizadas
        // Por ahora retornamos un ID simulado para continuar con la demostración
        try {
            return Integer.parseInt(cedula.substring(0, Math.min(cedula.length(), 8)));
        } catch (Exception e) {
            return null;
        }
    }
}