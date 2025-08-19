package conectaseguros.co.news_service.descuentos.domain.port.input;


import conectaseguros.co.news_service.descuentos.domain.model.LineaDescuento;
import conectaseguros.co.news_service.descuentos.domain.model.ResultadoValidacion;

public interface ValidadorLinea {

    /**
     * Valida una línea de descuento
     * @param linea Línea a validar
     * @param seccionalId Seccional que está procesando el archivo
     * @return Resultado de la validación
     */
    ResultadoValidacion validar(LineaDescuento linea, Integer seccionalId);

    /**
     * Establece el siguiente validador en la cadena
     * @param siguiente Siguiente validador
     * @return Este validador para permitir fluent interface
     */
    ValidadorLinea setSiguiente(ValidadorLinea siguiente);
}