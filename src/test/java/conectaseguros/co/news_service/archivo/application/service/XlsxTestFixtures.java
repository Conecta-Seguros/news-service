package conectaseguros.co.news_service.archivo.application.service;

import conectaseguros.co.news_service.archivo.domain.model.RangoUsadoExcel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Constructor de archivos XLSX sinteticos para pruebas, generados enteramente en memoria (sin
 * binarios fijados en el repositorio).
 *
 * <p>Consumido directamente por las pruebas de comportamiento de {@link LeerArchivoService} en
 * {@link LeerArchivoServiceXlsxTest}, y autoprobado por {@link XlsxTestFixturesTest} para
 * garantizar que cada variante reproduce la firma exacta que esas pruebas necesitan.
 */
public final class XlsxTestFixtures {

  /** Ruta interna del XML de la primera hoja dentro del paquete OOXML (zip). */
  static final String ENTRADA_HOJA_1 = "xl/worksheets/sheet1.xml";

  private XlsxTestFixtures() {}

  /** Archivo XLSX normal: encabezado + ~9 filas reales, sin rango usado fantasma. */
  public static byte[] normal() throws IOException {
    return workbookConFilas(10);
  }

  /**
   * Archivo XLSX legítimamente grande y denso: ~2000 filas reales (declarado ≈ poblado, ratio ≈ 1).
   * No debe ser rechazado por la heuristica de rango fantasma.
   */
  public static byte[] densoGrande() throws IOException {
    return workbookConFilas(2_000);
  }

  /**
   * Archivo XLSX con "rango usado fantasma": solo ~36 filas reales pobladas, pero el elemento
   * {@code <dimension>} de la hoja fue reescrito para declarar {@code A1:M1042782}, reproduciendo
   * la firma exacta reportada en produccion sin necesidad de generar un archivo de cientos de MB.
   * Excede el techo absoluto ({@link RangoUsadoExcel#FILAS_DECLARADAS_MAXIMAS}), por lo que se
   * rechaza en el chequeo fail-fast previo al parseo (variante por techo).
   */
  public static byte[] rangoFantasma() throws IOException {
    byte[] base = workbookConFilas(36);
    return reescribirDimension(base, "A1:M1042782");
  }

  /**
   * Archivo XLSX con "rango usado fantasma" sutil: ~100 filas reales pobladas, pero el {@code
   * <dimension>} fue reescrito para declarar {@code A1:M60000} (60.000 filas). Queda por debajo del
   * techo absoluto pero excede la proporcion maxima permitida, por lo que solo se detecta en la
   * verificacion posterior al parseo (variante por proporcion), no en el chequeo fail-fast.
   */
  public static byte[] rangoFantasmaBajoTecho() throws IOException {
    byte[] base = workbookConFilas(100);
    return reescribirDimension(base, "A1:M60000");
  }

  /**
   * Archivo XLSX cuyo XML de la primera hoja contiene, en el prologo (antes del elemento raiz), un
   * comentario XML gigante y extremadamente compresible (500.000 caracteres repetidos). Al
   * comprimir con DEFLATE, el ratio compresion/descompresion queda muy por debajo del 1% (umbral
   * por defecto {@code ZipSecureFile.MIN_INFLATE_RATIO}), reproduciendo la firma clasica de un "zip
   * bomb". No relacionado con la heuristica de rango usado fantasma: sirve exclusivamente para
   * verificar que la proteccion nativa de POI contra bombas zip ({@link
   * org.apache.poi.openxml4j.util.ZipSecureFile}) sigue activa al abrir el paquete OOXML.
   */
  public static byte[] bombaZip() throws IOException {
    byte[] base = workbookConFilas(2);
    String relleno = "A".repeat(500_000);
    return insertarComentarioProlog(base, relleno);
  }

  private static byte[] workbookConFilas(int filas) throws IOException {
    try (XSSFWorkbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Datos");
      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("CODEMPLEADO");
      header.createCell(1).setCellValue("VALOR");
      for (int i = 1; i < filas; i++) {
        Row row = sheet.createRow(i);
        row.createCell(0).setCellValue("EMP" + i);
        row.createCell(1).setCellValue(i * 100.0);
      }
      workbook.write(salida);
      return salida.toByteArray();
    }
  }

  private static byte[] reescribirDimension(byte[] xlsxOriginal, String nuevaRef)
      throws IOException {
    ByteArrayOutputStream salida = new ByteArrayOutputStream();
    try (ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(xlsxOriginal));
        ZipOutputStream zipOut = new ZipOutputStream(salida)) {
      ZipEntry entrada;
      while ((entrada = zipIn.getNextEntry()) != null) {
        byte[] contenido = zipIn.readAllBytes();
        if (entrada.getName().equals(ENTRADA_HOJA_1)) {
          contenido = reemplazarDimension(contenido, nuevaRef);
        }
        zipOut.putNextEntry(new ZipEntry(entrada.getName()));
        zipOut.write(contenido);
        zipOut.closeEntry();
      }
    }
    return salida.toByteArray();
  }

  private static byte[] reemplazarDimension(byte[] sheetXml, String nuevaRef) {
    String xml = new String(sheetXml, StandardCharsets.UTF_8);
    String reescrito =
        xml.replaceFirst("<dimension ref=\"[^\"]*\"", "<dimension ref=\"" + nuevaRef + "\"");
    return reescrito.getBytes(StandardCharsets.UTF_8);
  }

  private static byte[] insertarComentarioProlog(byte[] xlsxOriginal, String relleno)
      throws IOException {
    ByteArrayOutputStream salida = new ByteArrayOutputStream();
    try (ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(xlsxOriginal));
        ZipOutputStream zipOut = new ZipOutputStream(salida)) {
      ZipEntry entrada;
      while ((entrada = zipIn.getNextEntry()) != null) {
        byte[] contenido = zipIn.readAllBytes();
        if (entrada.getName().equals(ENTRADA_HOJA_1)) {
          contenido = insertarComentario(contenido, relleno);
        }
        zipOut.putNextEntry(new ZipEntry(entrada.getName()));
        zipOut.write(contenido);
        zipOut.closeEntry();
      }
    }
    return salida.toByteArray();
  }

  private static byte[] insertarComentario(byte[] sheetXml, String relleno) {
    String xml = new String(sheetXml, StandardCharsets.UTF_8);
    String comentario = "<!--" + relleno + "-->";
    String conComentario = xml.replaceFirst("(\\?>)", "$1" + comentario);
    return conComentario.getBytes(StandardCharsets.UTF_8);
  }
}
