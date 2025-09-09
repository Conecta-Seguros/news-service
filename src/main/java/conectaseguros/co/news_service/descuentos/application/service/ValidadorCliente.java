package conectaseguros.co.news_service.descuentos.application.service;

import conectaseguros.co.news_service.descuentos.domain.model.LineaDescuento;
import conectaseguros.co.news_service.descuentos.domain.model.ResultadoValidacion;
import conectaseguros.co.news_service.descuentos.domain.port.input.ValidadorLinea;
import conectaseguros.co.news_service.descuentos.domain.port.output.ClienteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ValidadorCliente implements ValidadorLinea {

    private static final Logger logger = LoggerFactory.getLogger(ValidadorCliente.class);

    private final ClienteService clienteService;
    private ValidadorLinea siguiente;

    public ValidadorCliente(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Override
    public ResultadoValidacion validar(LineaDescuento linea, Integer seccionalId, String periodo) {
        logger.debug("Validando cliente línea: {} - Cédula: {} - Período: {}",
                linea.getNumeroLinea(), linea.getCodigoEmpleado(), periodo);

        // Validar que el cliente existe y pertenece a la seccional
        ResultadoValidacion validacionSeccional = clienteService.validarClienteSeccional(
                linea.getCodigoEmpleado(), seccionalId
        );

        if (validacionSeccional.tieneErrores()) {
            logger.debug("Cliente {} no válido para seccional {}: {}",
                    linea.getCodigoEmpleado(), seccionalId,
                    validacionSeccional.getDescripcionErrores());
            return validacionSeccional;
        }

        // Validar que el cliente está activo
        ResultadoValidacion validacionActivo = clienteService.validarClienteActivo(
                linea.getCodigoEmpleado()
        );

        if (validacionActivo.tieneErrores()) {
            logger.debug("Cliente {} no está activo: {}",
                    linea.getCodigoEmpleado(), validacionActivo.getDescripcionErrores());
            return validacionActivo;
        }

        logger.debug("Cliente {} validado correctamente para seccional {} en período {}",
                linea.getCodigoEmpleado(), seccionalId, periodo);

        // Continuar con el siguiente validador
        if (siguiente != null) {
            return siguiente.validar(linea, seccionalId, periodo);
        }

        return ResultadoValidacion.exitoso();
    }

    @Override
    public ValidadorLinea setSiguiente(ValidadorLinea siguiente) {
        this.siguiente = siguiente;
        return this;
    }
}
