package conectaseguros.co.news_service.archivo.domain.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import conectaseguros.co.news_service.archivo.domain.model.RangoUsadoExcel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del dominio para la excepcion de rango usado desproporcionado ("rango usado fantasma").
 * Verifica jerarquia y el texto exacto de los dos mensajes de negocio definidos en el diseno.
 */
class ArchivoRangoUsadoInvalidoExceptionTest {

  @Test
  @DisplayName("hereda de ArchivoNoValidoException (mapeo 400 existente)")
  void heredaDeArchivoNoValidoException() {
    ArchivoRangoUsadoInvalidoException excepcion =
        ArchivoRangoUsadoInvalidoException.porRangoDesproporcionado(1_042_782L, 36L);

    assertThat(excepcion).isInstanceOf(ArchivoNoValidoException.class);
    assertThat(excepcion).isInstanceOf(ArchivoException.class);
  }

  @Test
  @DisplayName(
      "variante por techo excedido (filas pobladas desconocidas) omite la clausula de datos")
  void porRangoDesproporcionado_variantePorTecho() {
    ArchivoRangoUsadoInvalidoException excepcion =
        ArchivoRangoUsadoInvalidoException.porRangoDesproporcionado(
            1_042_782L, ArchivoRangoUsadoInvalidoException.FILAS_POBLADAS_DESCONOCIDAS);

    assertThat(excepcion.getMessage())
        .isEqualTo(
            "El archivo Excel declara un rango de celdas usado desproporcionado (más de"
                + " 200.000 filas declaradas). Esto suele deberse a formato aplicado sobre"
                + " miles de filas o columnas vacías ('rango usado fantasma'), normalmente"
                + " heredado al copiar y pegar datos desde otro archivo. Para corregirlo: abra"
                + " el archivo en Excel, presione Ctrl+Fin para ver la última celda usada,"
                + " elimine las filas y columnas vacías sobrantes (o use Inicio > Borrar >"
                + " Borrar formato en exceso), guarde de nuevo y vuelva a cargar el archivo.");
  }

  @Test
  @DisplayName("variante por proporcion invalida incluye ambos conteos formateados")
  void porRangoDesproporcionado_variantePorProporcion() {
    ArchivoRangoUsadoInvalidoException excepcion =
        ArchivoRangoUsadoInvalidoException.porRangoDesproporcionado(1_042_782L, 36L);

    assertThat(excepcion.getMessage())
        .isEqualTo(
            "El archivo Excel declara un rango de celdas usado desproporcionado (1.042.782"
                + " filas declaradas frente a 36 filas con datos). Esto suele deberse a formato"
                + " aplicado sobre miles de filas o columnas vacías ('rango usado fantasma'),"
                + " normalmente heredado al copiar y pegar datos desde otro archivo. Para"
                + " corregirlo: abra el archivo en Excel, presione Ctrl+Fin para ver la última"
                + " celda usada, elimine las filas y columnas vacías sobrantes (o use Inicio >"
                + " Borrar > Borrar formato en exceso), guarde de nuevo y vuelva a cargar el"
                + " archivo.");
  }

  @Test
  @DisplayName("la variante por techo usa la misma constante que RangoUsadoExcel")
  void porRangoDesproporcionado_usaConstanteCompartida() {
    ArchivoRangoUsadoInvalidoException excepcion =
        ArchivoRangoUsadoInvalidoException.porRangoDesproporcionado(
            RangoUsadoExcel.FILAS_DECLARADAS_MAXIMAS + 1,
            ArchivoRangoUsadoInvalidoException.FILAS_POBLADAS_DESCONOCIDAS);

    assertThat(excepcion.getMessage()).contains("más de 200.000 filas declaradas");
    assertThat(excepcion.getMessage()).doesNotContain("frente a");
  }
}
