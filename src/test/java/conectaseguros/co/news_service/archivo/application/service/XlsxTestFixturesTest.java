package conectaseguros.co.news_service.archivo.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import conectaseguros.co.news_service.archivo.domain.model.RangoUsadoExcel;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Autoprueba del constructor de fixtures XLSX sintetico. Garantiza que cada variante reproduce
 * exactamente la firma que las pruebas de comportamiento de {@link LeerArchivoService} (en {@link
 * LeerArchivoServiceXlsxTest}) necesitan.
 */
class XlsxTestFixturesTest {

  @Test
  @DisplayName("normal() produce un archivo XLSX valido con 10 filas reales")
  void normal_produceDiezFilasReales() throws IOException {
    byte[] contenido = XlsxTestFixtures.normal();

    try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(contenido))) {
      XSSFSheet sheet = workbook.getSheetAt(0);
      assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(10);
    }
  }

  @Test
  @DisplayName("densoGrande() produce ~2000 filas reales pobladas")
  void densoGrande_produceDosMilFilasReales() throws IOException {
    byte[] contenido = XlsxTestFixtures.densoGrande();

    try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(contenido))) {
      XSSFSheet sheet = workbook.getSheetAt(0);
      assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(2_000);
    }
  }

  @Test
  @DisplayName("rangoFantasma() reescribe el dimension ref manteniendo pocas filas reales")
  void rangoFantasma_reescribeDimensionYMantienePocasFilasReales() throws IOException {
    byte[] contenido = XlsxTestFixtures.rangoFantasma();

    String sheetXml = extraerEntradaZip(contenido, XlsxTestFixtures.ENTRADA_HOJA_1);
    assertThat(sheetXml).contains("<dimension ref=\"A1:M1042782\"");

    try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(contenido))) {
      XSSFSheet sheet = workbook.getSheetAt(0);
      assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(36);
    }
  }

  @Test
  @DisplayName("rangoFantasma() reproduce la firma que RangoUsadoExcel marca como fantasma")
  void rangoFantasma_reproduceFirmaDetectableComoFantasma() throws IOException {
    byte[] contenido = XlsxTestFixtures.rangoFantasma();
    String sheetXml = extraerEntradaZip(contenido, XlsxTestFixtures.ENTRADA_HOJA_1);

    String ref = extraerDimensionRef(sheetXml);
    long filasDeclaradas = RangoUsadoExcel.filasDeclaradas(ref);

    assertThat(filasDeclaradas).isEqualTo(1_042_782L);
    assertThat(RangoUsadoExcel.excedeCeiling(filasDeclaradas)).isTrue();
    assertThat(RangoUsadoExcel.esFantasma(filasDeclaradas, 36L)).isTrue();
  }

  @Test
  @DisplayName("bombaZip() produce un ratio de compresion muy por debajo del umbral de 1%")
  void bombaZip_produceRatioDeCompresionExtremo() throws IOException {
    byte[] contenido = XlsxTestFixtures.bombaZip();
    String sheetXml = extraerEntradaZip(contenido, XlsxTestFixtures.ENTRADA_HOJA_1);

    // El comentario gigante debe seguir siendo XML valido: > 500.000 caracteres, ratio nominal
    // de descompresion (comprimido/tamano-original) muy por debajo de 0.01 (1%).
    assertThat(sheetXml.length()).isGreaterThan(500_000);
    long tamanoComprimidoEntrada = tamanoComprimido(contenido, XlsxTestFixtures.ENTRADA_HOJA_1);
    double ratio =
        (double) tamanoComprimidoEntrada / sheetXml.getBytes(StandardCharsets.UTF_8).length;
    assertThat(ratio).isLessThan(0.01);
  }

  private static long tamanoComprimido(byte[] xlsx, String nombreEntrada) throws IOException {
    try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(xlsx))) {
      ZipEntry entrada;
      while ((entrada = zip.getNextEntry()) != null) {
        if (entrada.getName().equals(nombreEntrada)) {
          zip.readAllBytes();
          zip.closeEntry();
          return entrada.getCompressedSize();
        }
      }
    }
    throw new IllegalStateException("No se encontro la entrada " + nombreEntrada + " en el zip");
  }

  private static String extraerEntradaZip(byte[] xlsx, String nombreEntrada) throws IOException {
    try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(xlsx))) {
      ZipEntry entrada;
      while ((entrada = zip.getNextEntry()) != null) {
        if (entrada.getName().equals(nombreEntrada)) {
          InputStream in = zip;
          return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
      }
    }
    throw new IllegalStateException("No se encontro la entrada " + nombreEntrada + " en el zip");
  }

  private static String extraerDimensionRef(String sheetXml) {
    int inicio = sheetXml.indexOf("<dimension ref=\"") + "<dimension ref=\"".length();
    int fin = sheetXml.indexOf('"', inicio);
    return sheetXml.substring(inicio, fin);
  }
}
