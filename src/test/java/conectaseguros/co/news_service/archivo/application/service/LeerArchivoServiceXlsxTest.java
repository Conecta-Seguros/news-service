package conectaseguros.co.news_service.archivo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import conectaseguros.co.news_service.archivo.domain.exceptions.ArchivoNoValidoException;
import conectaseguros.co.news_service.archivo.domain.exceptions.ArchivoRangoUsadoInvalidoException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 * Pruebas de comportamiento de {@link LeerArchivoService#leerArchivoDesdeBytes(byte[], String)}
 * para el flujo XLSX: apertura seekable protegida contra zip-bombs (seccion "apertura seekable"),
 * deteccion de "rango usado fantasma" (seccion "deteccion de rango fantasma"), no envolvimiento de
 * la excepcion de dominio (seccion "propagacion sin envolver") y verificacion de alcance /
 * comportamiento compartido entre los flujos de novedades y descuentos (seccion "verificacion de
 * flujo compartido").
 *
 * <p>Ninguno de los metodos bajo prueba ({@code leerArchivoDesdeBytes}) usa los repositorios ni el
 * almacenador inyectados en el constructor, por lo que se instancia el servicio con {@code null} en
 * esas dependencias en lugar de crear mocks sin aserciones.
 */
class LeerArchivoServiceXlsxTest {

  private final LeerArchivoService servicio = new LeerArchivoService(null, null, null);

  private ListAppender<ILoggingEvent> capturaDeLogs;
  private Logger loggerDelServicio;

  @BeforeEach
  void engancharCapturaDeLogs() {
    loggerDelServicio = (Logger) LoggerFactory.getLogger(LeerArchivoService.class);
    capturaDeLogs = new ListAppender<>();
    capturaDeLogs.start();
    loggerDelServicio.addAppender(capturaDeLogs);
  }

  @AfterEach
  void desengancharCapturaDeLogs() {
    loggerDelServicio.detachAppender(capturaDeLogs);
  }

  // ---- apertura seekable (pruebas de aprobacion / red de seguridad) ----

  @Test
  @DisplayName(
      "normal(): todas las filas y celdas se leen correctamente tras el cambio a canal seekable")
  void normal_seLeenTodasLasFilasYCeldasCorrectamente() throws IOException {
    byte[] contenido = XlsxTestFixtures.normal();

    List<String[]> filas = servicio.leerArchivoDesdeBytes(contenido, "novedades.xlsx");

    assertThat(filas).hasSize(10);
    assertThat(filas.get(0)).containsExactly("CODEMPLEADO", "VALOR");
    assertThat(filas.get(9)[0]).isEqualTo("EMP9");
    assertThat(Double.parseDouble(filas.get(9)[1])).isEqualTo(900.0);
  }

  @Test
  @DisplayName(
      "densoGrande(): ~2000 filas reales se leen completas y sin excepcion (declarado ~= poblado)")
  void densoGrande_seLeenTodasLasFilasSinExcepcion() throws IOException {
    byte[] contenido = XlsxTestFixtures.densoGrande();

    List<String[]> filas = servicio.leerArchivoDesdeBytes(contenido, "novedades.xlsx");

    assertThat(filas).hasSize(2_000);
    assertThat(filas.get(1_999)[0]).isEqualTo("EMP1999");
  }

  @Test
  @DisplayName(
      "bombaZip(): la apertura seekable sigue protegida contra bombas zip (ZipSecureFile), no se"
          + " lee sin limite un ratio de compresion extremo")
  void bombaZip_seRechazaPorProteccionNativaDePoiContraBombasZip() throws IOException {
    byte[] contenido = XlsxTestFixtures.bombaZip();

    Throwable excepcion =
        catchThrowable(() -> servicio.leerArchivoDesdeBytes(contenido, "novedades.xlsx"));

    assertThat(excepcion)
        .isInstanceOf(ArchivoNoValidoException.class)
        .hasMessageContaining("Zip bomb detected");
  }

  // ---- deteccion de rango usado fantasma ----

  @Test
  @DisplayName(
      "rangoFantasma(): se rechaza fail-fast (variante por techo) antes de terminar el parseo")
  void rangoFantasma_lanzaExcepcionDeDominioPorTecho() throws IOException {
    byte[] contenido = XlsxTestFixtures.rangoFantasma();
    String mensajeEsperado =
        ArchivoRangoUsadoInvalidoException.porRangoDesproporcionado(
                1_042_782L, ArchivoRangoUsadoInvalidoException.FILAS_POBLADAS_DESCONOCIDAS)
            .getMessage();

    assertThatThrownBy(() -> servicio.leerArchivoDesdeBytes(contenido, "novedades.xlsx"))
        .isInstanceOf(ArchivoRangoUsadoInvalidoException.class)
        .hasMessage(mensajeEsperado);
  }

  @Test
  @DisplayName(
      "rangoFantasmaBajoTecho(): se rechaza tras el parseo (variante por proporcion) cuando queda"
          + " bajo el techo absoluto pero excede la proporcion maxima")
  void rangoFantasmaBajoTecho_lanzaExcepcionDeDominioPorProporcion() throws IOException {
    byte[] contenido = XlsxTestFixtures.rangoFantasmaBajoTecho();
    String mensajeEsperado =
        ArchivoRangoUsadoInvalidoException.porRangoDesproporcionado(60_000L, 100L).getMessage();

    assertThatThrownBy(() -> servicio.leerArchivoDesdeBytes(contenido, "novedades.xlsx"))
        .isInstanceOf(ArchivoRangoUsadoInvalidoException.class)
        .hasMessage(mensajeEsperado);
  }

  @Test
  @DisplayName(
      "rangoFantasma(): el rechazo por techo se registra en WARN con el nombre del archivo y las"
          + " filas declaradas (diagnosticable en produccion)")
  void rangoFantasma_registraRechazoEnLogConNombreArchivoYFilasDeclaradas() throws IOException {
    byte[] contenido = XlsxTestFixtures.rangoFantasma();

    catchThrowable(() -> servicio.leerArchivoDesdeBytes(contenido, "novedades-problema.xlsx"));

    assertThat(capturaDeLogs.list)
        .anySatisfy(
            evento -> {
              assertThat(evento.getLevel()).isEqualTo(Level.WARN);
              assertThat(evento.getFormattedMessage()).contains("novedades-problema.xlsx");
              assertThat(evento.getFormattedMessage()).contains("1042782");
            });
  }

  @Test
  @DisplayName(
      "rangoFantasmaBajoTecho(): el rechazo por proporcion se registra en WARN con el nombre del"
          + " archivo y ambos conteos (declaradas y pobladas)")
  void rangoFantasmaBajoTecho_registraRechazoEnLogConNombreArchivoYAmbosConteos()
      throws IOException {
    byte[] contenido = XlsxTestFixtures.rangoFantasmaBajoTecho();

    catchThrowable(() -> servicio.leerArchivoDesdeBytes(contenido, "descuentos-problema.xlsx"));

    assertThat(capturaDeLogs.list)
        .anySatisfy(
            evento -> {
              assertThat(evento.getLevel()).isEqualTo(Level.WARN);
              assertThat(evento.getFormattedMessage()).contains("descuentos-problema.xlsx");
              assertThat(evento.getFormattedMessage()).contains("60000");
              assertThat(evento.getFormattedMessage()).contains("100");
            });
  }

  // ---- propagacion sin envolver: la excepcion de dominio no se envuelve ----

  @Test
  @DisplayName(
      "la excepcion de rango fantasma sobrevive intacta fuera de leerArchivoDesdeBytes, no se"
          + " envuelve en un IOException/ArchivoNoValidoException generico")
  void rangoFantasma_laExcepcionDeDominioSobrevivePropagacionSinEnvolver() throws IOException {
    byte[] contenido = XlsxTestFixtures.rangoFantasma();

    assertThatThrownBy(() -> servicio.leerArchivoDesdeBytes(contenido, "novedades.xlsx"))
        .isInstanceOf(ArchivoRangoUsadoInvalidoException.class)
        .hasMessageNotContaining("Error al procesar el archivo")
        .hasMessageContaining("más de 200.000 filas declaradas");
  }

  // ---- verificacion de flujo compartido: alcance (.xls sin cambios) y comportamiento
  // compartido entre novedades y descuentos ----

  @Test
  @DisplayName(
      ".xls (OLE2/HSSF) no se ve afectado: no se aplica ninguna deteccion de rango fantasma")
  void archivoXls_noSeAfectaPorLaDeteccionDeRangoFantasma() throws IOException {
    byte[] contenido = construirXlsValido();

    List<String[]> filas = servicio.leerArchivoDesdeBytes(contenido, "novedades.xls");

    assertThat(filas).hasSize(4);
    assertThat(filas.get(0)).containsExactly("CODEMPLEADO", "VALOR");
    assertThat(filas.get(3)[0]).isEqualTo("EMP3");
  }

  @Test
  @DisplayName(
      "un archivo normal produce el mismo resultado sin importar si lo invoca el flujo de"
          + " novedades o el de descuentos (ambos comparten leerArchivoDesdeBytes)")
  void flujoCompartido_normalProduceMismoResultadoEnAmbosFlujos() throws IOException {
    byte[] contenido = XlsxTestFixtures.normal();

    // Simula la invocacion desde ArchivoController (novedades): bytes ya en memoria, sin path.
    List<String[]> resultadoNovedades = servicio.leerArchivoDesdeBytes(contenido, "novedades.xlsx");
    // Simula la invocacion desde ProcesarDescuentosService.procesarArchivoSincrono: mismo puerto,
    // mismos bytes recuperados del almacenador, distinto nombre de archivo de origen.
    List<String[]> resultadoDescuentos =
        servicio.leerArchivoDesdeBytes(contenido, "descuentos.xlsx");

    assertThat(resultadoNovedades).hasSize(10);
    assertThat(resultadoDescuentos).hasSize(10);
    assertThat(resultadoNovedades.get(9)).isEqualTo(resultadoDescuentos.get(9));
  }

  @Test
  @DisplayName(
      "un archivo con rango fantasma es rechazado identicamente sin importar si lo invoca el"
          + " flujo de novedades o el de descuentos")
  void flujoCompartido_rangoFantasmaLanzaMismaExcepcionEnAmbosFlujos() throws IOException {
    byte[] contenido = XlsxTestFixtures.rangoFantasma();

    Throwable excepcionNovedades =
        catchThrowable(() -> servicio.leerArchivoDesdeBytes(contenido, "novedades.xlsx"));
    Throwable excepcionDescuentos =
        catchThrowable(() -> servicio.leerArchivoDesdeBytes(contenido, "descuentos.xlsx"));

    assertThat(excepcionNovedades)
        .isInstanceOf(ArchivoRangoUsadoInvalidoException.class)
        .hasMessage(excepcionDescuentos.getMessage());
    assertThat(excepcionDescuentos).isInstanceOf(ArchivoRangoUsadoInvalidoException.class);
  }

  /** Construye un archivo .xls (OLE2/HSSF) valido: encabezado + 3 filas reales. */
  private static byte[] construirXlsValido() throws IOException {
    try (HSSFWorkbook workbook = new HSSFWorkbook();
        ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Datos");
      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("CODEMPLEADO");
      header.createCell(1).setCellValue("VALOR");
      for (int i = 1; i <= 3; i++) {
        Row row = sheet.createRow(i);
        row.createCell(0).setCellValue("EMP" + i);
        row.createCell(1).setCellValue(i * 100.0);
      }
      workbook.write(salida);
      return salida.toByteArray();
    }
  }
}
