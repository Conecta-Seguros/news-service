package conectaseguros.co.news_service.descuentos.application.service;

import conectaseguros.co.news_service.descuentos.domain.model.ErrorValidacion;
import conectaseguros.co.news_service.descuentos.domain.model.LineaDescuento;
import conectaseguros.co.news_service.descuentos.domain.model.ResultadoValidacion;
import conectaseguros.co.news_service.descuentos.domain.model.enums.TipoError;
import conectaseguros.co.news_service.descuentos.domain.port.input.ValidadorLinea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ValidadorFormatoLinea implements ValidadorLinea {

    private static final Logger logger = LoggerFactory.getLogger(ValidadorFormatoLinea.class);

    // Nombres de campos obligatorios centralizados
    private static final String CAMPO_TERCERO = "TERCERO";
    private static final String CAMPO_NIT = "NIT";
    private static final String CAMPO_CODCONCEPTO = "CODCONCEPTO";
    private static final String CAMPO_CODEMPLEADO = "CODEMPLEADO";
    private static final String CAMPO_NOMBRE1 = "Nombre1";
    private static final String CAMPO_APELLIDO1 = "Apellido1";
    private static final String CAMPO_COD_UNIDAD_EJECUTORA = "COD_UNIDAD_EJECUTORA";
    private static final String CAMPO_VALOR = "VALOR";
    private static final String CAMPO_NOMINA = "NOMINA";

    private ValidadorLinea siguiente;

    @Override
    public ResultadoValidacion validar(LineaDescuento linea, Integer seccionalId, String periodo) {
        logger.debug("Validando formato de línea: {} - Período: {}", linea.getNumeroLinea(), periodo);

        List<ErrorValidacion> errores = new ArrayList<>();

        // Validar campos obligatorios adicionales
        validarCamposObligatorios(linea, errores);

        // Validar longitudes
        validarLongitudesCampos(linea, errores);

        // Validar formatos específicos
        validarFormatosEspecificos(linea, errores);

        if (!errores.isEmpty()) {
            logger.debug("Línea {} tiene errores de formato: {}", linea.getNumeroLinea(), errores.size());
            return ResultadoValidacion.conErrores(errores);
        }

        logger.debug("Formato de línea {} validado correctamente para período {}",
                linea.getNumeroLinea(), periodo);

        // Si no hay errores, continuar con el siguiente validador
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

    private void validarCamposObligatorios(LineaDescuento linea, List<ErrorValidacion> errores) {
        if (linea.getTercero() == null || linea.getTercero().trim().isEmpty()) {
            errores.add(new ErrorValidacion(
                    TipoError.CAMPO_OBLIGATORIO_FALTANTE,
                    String.format("Línea %d: Campo %s es obligatorio", linea.getNumeroLinea(), CAMPO_TERCERO),
                    CAMPO_TERCERO
            ));
        }
        if (linea.getNit() == null || linea.getNit().trim().isEmpty()) {
            errores.add(new ErrorValidacion(
                    TipoError.CAMPO_OBLIGATORIO_FALTANTE,
                    String.format("Línea %d: Campo %s es obligatorio", linea.getNumeroLinea(), CAMPO_NIT),
                    CAMPO_NIT
            ));
        }
        if (linea.getCodigoConcepto() == null || linea.getCodigoConcepto().trim().isEmpty()) {
            errores.add(new ErrorValidacion(
                    TipoError.CAMPO_OBLIGATORIO_FALTANTE,
                    String.format("Línea %d: Campo %s es obligatorio", linea.getNumeroLinea(), CAMPO_CODCONCEPTO),
                    CAMPO_CODCONCEPTO
            ));
        }
        // Puedes agregar aquí validaciones para otros campos obligatorios si lo requieres
    }

    private void validarLongitudesCampos(LineaDescuento linea, List<ErrorValidacion> errores) {
        if (linea.getTercero() != null && linea.getTercero().length() > 100) {
            errores.add(new ErrorValidacion(
                    TipoError.LONGITUD_CAMPO_INVALIDA,
                    String.format("Línea %d: Campo TERCERO excede 100 caracteres", linea.getNumeroLinea()),
                    "TERCERO",
                    linea.getTercero()
            ));
        }

        if (linea.getNit() != null && linea.getNit().length() > 20) {
            errores.add(new ErrorValidacion(
                    TipoError.LONGITUD_CAMPO_INVALIDA,
                    String.format("Línea %d: Campo NIT excede 20 caracteres", linea.getNumeroLinea()),
                    "NIT",
                    linea.getNit()
            ));
        }

        if (linea.getCodigoConcepto() != null && linea.getCodigoConcepto().length() > 50) {
            errores.add(new ErrorValidacion(
                    TipoError.LONGITUD_CAMPO_INVALIDA,
                    String.format("Línea %d: Campo CODCONCEPTO excede 50 caracteres", linea.getNumeroLinea()),
                    "CODCONCEPTO",
                    linea.getCodigoConcepto()
            ));
        }
    }

    private void validarFormatosEspecificos(LineaDescuento linea, List<ErrorValidacion> errores) {
        // Validar formato del NIT
        if (linea.getNit() != null && !linea.getNit().matches("\\d{9,15}")) {
            errores.add(new ErrorValidacion(
                    TipoError.FORMATO_NUMERO_INVALIDO,
                    String.format("Línea %d: NIT debe ser numérico de 9-15 dígitos", linea.getNumeroLinea()),
                    "NIT",
                    linea.getNit()
            ));
        }

        // Validar código de unidad ejecutora
        if (linea.getCodigoUnidadEjecutora() != null && !linea.getCodigoUnidadEjecutora().matches("\\d+")) {
            errores.add(new ErrorValidacion(
                    TipoError.FORMATO_NUMERO_INVALIDO,
                    String.format("Línea %d: COD_UNIDAD_EJECUTORA debe ser numérico", linea.getNumeroLinea()),
                    "COD_UNIDAD_EJECUTORA",
                    linea.getCodigoUnidadEjecutora()
            ));
        }
    }
}