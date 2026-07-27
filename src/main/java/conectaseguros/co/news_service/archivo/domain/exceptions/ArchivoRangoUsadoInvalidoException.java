package conectaseguros.co.news_service.archivo.domain.exceptions;

import conectaseguros.co.news_service.archivo.domain.model.RangoUsadoExcel;
import java.util.Locale;

/**
 * Se lanza cuando un archivo Excel (XLSX) declara un rango de celdas usado (atributo {@code ref}
 * del elemento {@code <dimension>} de la hoja) desproporcionado respecto a las filas realmente
 * pobladas ("rango usado fantasma"). Hereda el mapeo HTTP 400 existente de {@link
 * ArchivoNoValidoException}, por lo que no requiere cambios en el manejador global de excepciones.
 */
public class ArchivoRangoUsadoInvalidoException extends ArchivoNoValidoException {

  /**
   * Valor centinela para {@code filasPobladas} cuando aun no se conoce: se usa en el rechazo
   * fail-fast por techo absoluto, antes de contar filas pobladas.
   */
  public static final long FILAS_POBLADAS_DESCONOCIDAS = -1L;

  private static final Locale LOCALE_MENSAJES = Locale.of("es", "CO");

  private static final String SUGERENCIA_CORRECCION =
      "Esto suele deberse a formato aplicado sobre miles de filas o columnas vacías"
          + " ('rango usado fantasma'), normalmente heredado al copiar y pegar datos desde"
          + " otro archivo. Para corregirlo: abra el archivo en Excel, presione Ctrl+Fin para"
          + " ver la última celda usada, elimine las filas y columnas vacías sobrantes (o use"
          + " Inicio > Borrar > Borrar formato en exceso), guarde de nuevo y vuelva a cargar"
          + " el archivo.";

  public ArchivoRangoUsadoInvalidoException(String mensaje) {
    super(mensaje);
  }

  /**
   * Construye la excepcion con el mensaje adecuado segun si las filas realmente pobladas ya se
   * conocen. Pase {@link #FILAS_POBLADAS_DESCONOCIDAS} cuando el rechazo ocurre por techo absoluto
   * (fail-fast, antes de contar filas pobladas).
   */
  public static ArchivoRangoUsadoInvalidoException porRangoDesproporcionado(
      long filasDeclaradas, long filasPobladas) {
    String mensaje =
        filasPobladas == FILAS_POBLADAS_DESCONOCIDAS
            ? mensajePorTecho()
            : mensajePorProporcion(filasDeclaradas, filasPobladas);
    return new ArchivoRangoUsadoInvalidoException(mensaje);
  }

  private static String mensajePorTecho() {
    return String.format(
        LOCALE_MENSAJES,
        "El archivo Excel declara un rango de celdas usado desproporcionado (más de %,d"
            + " filas declaradas). %s",
        RangoUsadoExcel.FILAS_DECLARADAS_MAXIMAS,
        SUGERENCIA_CORRECCION);
  }

  private static String mensajePorProporcion(long filasDeclaradas, long filasPobladas) {
    return String.format(
        LOCALE_MENSAJES,
        "El archivo Excel declara un rango de celdas usado desproporcionado (%,d filas"
            + " declaradas frente a %,d filas con datos). %s",
        filasDeclaradas,
        filasPobladas,
        SUGERENCIA_CORRECCION);
  }
}
