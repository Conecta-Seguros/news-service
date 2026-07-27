package conectaseguros.co.news_service.archivo.application.service;

import conectaseguros.co.news_service.archivo.domain.exceptions.ArchivoNoEncontradoException;
import conectaseguros.co.news_service.archivo.domain.exceptions.ArchivoNoValidoException;
import conectaseguros.co.news_service.archivo.domain.exceptions.ArchivoRangoUsadoInvalidoException;
import conectaseguros.co.news_service.archivo.domain.model.ArchivoDescuentos;
import conectaseguros.co.news_service.archivo.domain.model.ArchivoNovedades;
import conectaseguros.co.news_service.archivo.domain.model.RangoUsadoExcel;
import conectaseguros.co.news_service.archivo.domain.model.enums.TipoArchivo;
import conectaseguros.co.news_service.archivo.domain.port.input.LeerArchivoUseCase;
import conectaseguros.co.news_service.archivo.domain.port.output.AlmacenadorArchivos;
import conectaseguros.co.news_service.archivo.domain.port.output.ArchivoDescuentosRepository;
import conectaseguros.co.news_service.archivo.domain.port.output.ArchivoNovedadesRepository;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.xml.parsers.SAXParserFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.util.ZipEntrySource;
import org.apache.poi.openxml4j.util.ZipFileZipEntrySource;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

@Service
public class LeerArchivoService implements LeerArchivoUseCase {

  private static final Logger logger = LoggerFactory.getLogger(LeerArchivoService.class);

  private final ArchivoNovedadesRepository archivoNovedadesRepository;
  private final ArchivoDescuentosRepository archivoDescuentosRepository;
  private final AlmacenadorArchivos almacenadorArchivos;

  public LeerArchivoService(
      ArchivoNovedadesRepository archivoNovedadesRepository,
      ArchivoDescuentosRepository archivoDescuentosRepository,
      AlmacenadorArchivos almacenadorArchivos) {
    this.archivoNovedadesRepository = archivoNovedadesRepository;
    this.archivoDescuentosRepository = archivoDescuentosRepository;
    this.almacenadorArchivos = almacenadorArchivos;
  }

  @Override
  public List<String[]> leerArchivo(Long archivoId, TipoArchivo tipoArchivo) {
    logger.info("Iniciando lectura de archivo ID: {} de tipo: {}", archivoId, tipoArchivo);

    byte[] contenido = obtenerContenidoArchivo(archivoId, tipoArchivo);
    String nombreArchivo = obtenerNombreArchivo(archivoId, tipoArchivo);

    return leerArchivoDesdeBytes(contenido, nombreArchivo);
  }

  /** Convierte una lista de arrays de String (filas) en una lista de líneas CSV (String) */
  private List<String> convertirFilasACSV(List<String[]> filas) {
    List<String> lineas = new ArrayList<>();
    for (String[] fila : filas) {
      lineas.add(String.join(",", fila));
    }
    return lineas;
  }

  /** Lee el archivo desde bytes y lo convierte en líneas de texto (CSV) para procesamiento */
  public List<String> leerArchivoComoLineas(Long archivoId, TipoArchivo tipoArchivo) {
    logger.info(
        "Iniciando lectura de archivo como líneas ID: {} de tipo: {}", archivoId, tipoArchivo);
    byte[] contenido = obtenerContenidoArchivo(archivoId, tipoArchivo);
    String nombreArchivo = obtenerNombreArchivo(archivoId, tipoArchivo);
    List<String[]> filas = leerArchivoDesdeBytes(contenido, nombreArchivo);
    return convertirFilasACSV(filas);
  }

  @Override
  public List<String[]> leerArchivoDesdeBytes(byte[] contenido, String nombreArchivo) {
    if (contenido == null || contenido.length == 0) {
      throw new ArchivoNoValidoException("El contenido del archivo está vacío");
    }

    try {
      String extension = obtenerExtension(nombreArchivo);

      switch (extension.toLowerCase()) {
        case "csv":
        case "txt":
          return leerArchivoTexto(contenido);
        case "xlsx":
          return leerArchivoExcelXLSX(contenido, nombreArchivo);
        case "xls":
          return leerArchivoExcelXLS(contenido);
        default:
          throw new ArchivoNoValidoException("Formato de archivo no soportado: " + extension);
      }
    } catch (IOException e) {
      logger.error("Error al leer archivo: {}", nombreArchivo, e);
      throw new ArchivoNoValidoException("Error al procesar el archivo: " + e.getMessage());
    }
  }

  public byte[] obtenerContenidoArchivo(Long archivoId, TipoArchivo tipoArchivo) {
    switch (tipoArchivo) {
      case NOVEDADES:
        ArchivoNovedades archivoNovedades =
            archivoNovedadesRepository
                .buscarPorId(archivoId)
                .orElseThrow(
                    () ->
                        new ArchivoNoEncontradoException(
                            "Archivo de novedades no encontrado con ID: " + archivoId));
        return almacenadorArchivos.recuperar(archivoNovedades.getRutaArchivo());

      case DESCUENTOS:
        ArchivoDescuentos archivoDescuentos =
            archivoDescuentosRepository
                .buscarPorId(archivoId)
                .orElseThrow(
                    () ->
                        new ArchivoNoEncontradoException(
                            "Archivo de descuentos no encontrado con ID: " + archivoId));
        return almacenadorArchivos.recuperar(archivoDescuentos.getRutaArchivo());

      default:
        throw new ArchivoNoValidoException("Tipo de archivo no soportado: " + tipoArchivo);
    }
  }

  public String obtenerNombreArchivo(Long archivoId, TipoArchivo tipoArchivo) {
    switch (tipoArchivo) {
      case NOVEDADES:
        return archivoNovedadesRepository
            .buscarPorId(archivoId)
            .map(ArchivoNovedades::getNombreArchivo)
            .orElse("archivo.csv");
      case DESCUENTOS:
        return archivoDescuentosRepository
            .buscarPorId(archivoId)
            .map(ArchivoDescuentos::getNombreArchivo)
            .orElse("archivo.csv");
      default:
        return "archivo.csv";
    }
  }

  private List<String[]> leerArchivoTexto(byte[] contenido) throws IOException {
    List<String[]> filas = new ArrayList<>();

    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(new ByteArrayInputStream(contenido), StandardCharsets.UTF_8))) {

      String linea;
      int numeroLinea = 0;

      while ((linea = reader.readLine()) != null) {
        numeroLinea++;

        if (linea.trim().isEmpty()) {
          logger.debug("Saltando línea vacía número: {}", numeroLinea);
          continue;
        }

        // Detectar separador (coma, punto y coma, tabulación)
        String[] campos = dividirLinea(linea);
        filas.add(campos);
      }
    }

    logger.info("Se leyeron {} filas del archivo de texto", filas.size());
    return filas;
  }

  /**
   * Crea el archivo temporal usado por {@link #leerArchivoExcelXLSX} con permisos restrictivos
   * (solo el propietario puede leerlo/escribirlo), ya que su contenido es el archivo completo
   * subido por el usuario (potencialmente datos de clientes/polizas). En sistemas de archivos POSIX
   * se usa el permiso exacto rw-------; en el resto (p. ej. Windows en desarrollo local) se cae a
   * un ajuste equivalente vía {@link File#setReadable}/{@link File#setWritable}.
   */
  private static Path crearArchivoTemporalSeguro() throws IOException {
    try {
      var soloPropietario =
          PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"));
      return Files.createTempFile("news-service-xlsx-", ".tmp", soloPropietario);
    } catch (UnsupportedOperationException sistemaNoPosix) {
      Path archivoTemporal = Files.createTempFile("news-service-xlsx-", ".tmp");
      File archivo = archivoTemporal.toFile();
      archivo.setReadable(false, false);
      archivo.setWritable(false, false);
      archivo.setReadable(true, true);
      archivo.setWritable(true, true);
      return archivoTemporal;
    }
  }

  private String[] dividirLinea(String linea) {
    // Detectar el separador más común
    String separador = ",";
    if (linea.contains(";") && linea.split(";").length > linea.split(",").length) {
      separador = ";";
    } else if (linea.contains("\t")) {
      separador = "\t";
    }

    // División simple, se puede mejorar para manejar comillas
    return linea.split(separador, -1);
  }

  private List<String[]> leerArchivoExcelXLSX(byte[] contenido, String nombreArchivo)
      throws IOException {
    List<String[]> filas = new ArrayList<>();
    // Contadores de la heuristica de "rango usado fantasma" (ver RangoUsadoExcel): actualizados
    // por el interceptor de <dimension> (declaradas) y por endRow (pobladas), ambos fuera del
    // catch que envuelve excepciones genericas, para que la verificacion posterior al parseo
    // pueda lanzar limpiamente.
    ContadorRangoUsado contador = new ContadorRangoUsado();

    // Apertura seekable respaldada por un archivo temporal: ZipSecureFile -- la subclase de POI
    // que activa la proteccion nativa contra "zip bombs" (ver ZipArchiveThresholdInputStream:
    // umbral de ratio de compresion y de tamano maximo por entrada) -- solo expone constructores
    // File/String, no un SeekableByteChannel/Builder como el ZipFile plano de commons-compress
    // que se usaba antes. Escribir el contenido (ya en memoria) a un archivo temporal permite
    // construir un ZipSecureFile real -- preservando esa proteccion -- sin volver al viejo
    // OPCPackage.open(InputStream) no seekable que causaba el crash de asignacion original: el
    // XML de la hoja se sigue leyendo en streaming (SAX) mediante acceso aleatorio al archivo, no
    // se buferiza en memoria completo. El archivo temporal se borra en el finally, exista o no
    // una excepcion.
    Path archivoTemporal = crearArchivoTemporalSeguro();
    try {
      Files.write(archivoTemporal, contenido);
      try (ZipSecureFile zip = new ZipSecureFile(archivoTemporal.toFile());
          ZipEntrySource fuenteZip = new ZipFileZipEntrySource(zip);
          OPCPackage pkg = OPCPackage.open(fuenteZip)) {
        XSSFReader xssfReader = new XSSFReader(pkg);
        SharedStrings sharedStrings = xssfReader.getSharedStringsTable();
        StylesTable stylesTable = xssfReader.getStylesTable();

        SheetContentsHandler handler =
            new SheetContentsHandler() {
              private final TreeMap<Integer, String> fila = new TreeMap<>();

              @Override
              public void startRow(int rowNum) {
                fila.clear();
              }

              @Override
              public void endRow(int rowNum) {
                if (!fila.isEmpty()) {
                  contador.filasPobladas.incrementAndGet();
                  int maxCol = fila.lastKey() + 1;
                  String[] campos = new String[maxCol];
                  for (int i = 0; i < maxCol; i++) {
                    campos[i] = fila.getOrDefault(i, "");
                  }
                  filas.add(campos);
                }
              }

              @Override
              public void cell(String cellRef, String valor, XSSFComment comment) {
                if (cellRef != null) {
                  String colStr = cellRef.replaceAll("[0-9]", "");
                  int col = CellReference.convertColStringToIndex(colStr);
                  fila.put(col, valor != null ? valor : "");
                }
              }
            };

        ContentHandler xssfContentHandler =
            new XSSFSheetXMLHandler(stylesTable, sharedStrings, handler, false);
        ContentHandler contentHandler =
            interceptarDimension(xssfContentHandler, contador.filasDeclaradas);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XMLReader xmlReader = factory.newSAXParser().getXMLReader();
        xmlReader.setContentHandler(contentHandler);

        Iterator<InputStream> sheets = xssfReader.getSheetsData();
        if (sheets.hasNext()) {
          try (InputStream sheetStream = sheets.next()) {
            xmlReader.parse(new InputSource(sheetStream));
          }
        }
      } catch (ArchivoRangoUsadoInvalidoException e) {
        // Rechazo fail-fast por techo absoluto (ver interceptarDimension): la excepcion de
        // dominio no debe ser envuelta por el catch generico de mas abajo, se propaga sin
        // modificar. Se registra aqui (no dentro del interceptor SAX) para mantener nombreArchivo
        // fuera del helper estatico sin estado.
        logger.warn(
            "Archivo Excel rechazado por rango usado fantasma (variante techo): archivo={},"
                + " filasDeclaradas={}",
            nombreArchivo,
            contador.filasDeclaradas.get());
        throw e;
      } catch (IOException e) {
        throw e;
      } catch (Exception e) {
        throw new IOException("Error al leer archivo XLSX (streaming SAX): " + e.getMessage(), e);
      }
    } finally {
      Files.deleteIfExists(archivoTemporal);
    }

    // Verificacion posterior al parseo: cubre archivos que quedan bajo el techo absoluto pero
    // cuya proporcion declarado/poblado sigue siendo desproporcionada (ver RangoUsadoExcel).
    contador.verificarNoEsFantasma(nombreArchivo);

    logger.info("Se leyeron {} filas del archivo Excel XLSX (streaming SAX)", filas.size());
    return filas;
  }

  /**
   * Contadores mutables de la heuristica de "rango usado fantasma" (ver {@link RangoUsadoExcel}):
   * {@code filasDeclaradas} es actualizado por el interceptor de {@code <dimension>} y {@code
   * filasPobladas} por {@code endRow}, durante el parseo SAX de {@link
   * #leerArchivoExcelXLSX(byte[], String)}. {@link #verificarNoEsFantasma(String)} aplica la
   * verificacion posterior al parseo (variante por proporcion) una vez terminado.
   */
  private static final class ContadorRangoUsado {
    private final AtomicLong filasDeclaradas = new AtomicLong();
    private final AtomicLong filasPobladas = new AtomicLong();

    private void verificarNoEsFantasma(String nombreArchivo) {
      long declaradas = filasDeclaradas.get();
      long pobladas = filasPobladas.get();
      if (RangoUsadoExcel.esFantasma(declaradas, pobladas)) {
        logger.warn(
            "Archivo Excel rechazado por rango usado fantasma (variante proporcion):"
                + " archivo={}, filasDeclaradas={}, filasPobladas={}",
            nombreArchivo,
            declaradas,
            pobladas);
        throw ArchivoRangoUsadoInvalidoException.porRangoDesproporcionado(declaradas, pobladas);
      }
    }
  }

  /**
   * Envuelve el {@link ContentHandler} real de POI para interceptar el elemento {@code <dimension>}
   * de la hoja (siempre el primer hijo del elemento raiz, antes de cualquier {@code <row>}):
   * captura el numero de filas declaradas y aborta el parseo fail-fast si excede el techo absoluto
   * configurado en {@link RangoUsadoExcel}.
   *
   * <p>Solo {@code startElement} tiene logica real; el resto de los 10 metodos de {@link
   * ContentHandler} delegan sin modificar al handler real (boilerplate de decorador requerido por
   * la interfaz SAX).
   */
  private static ContentHandler interceptarDimension(
      ContentHandler delegado, AtomicLong filasDeclaradas) {
    return new ContentHandler() {
      @Override
      public void setDocumentLocator(Locator locator) {
        delegado.setDocumentLocator(locator);
      }

      @Override
      public void startDocument() throws SAXException {
        delegado.startDocument();
      }

      @Override
      public void endDocument() throws SAXException {
        delegado.endDocument();
      }

      @Override
      public void startPrefixMapping(String prefix, String uri) throws SAXException {
        delegado.startPrefixMapping(prefix, uri);
      }

      @Override
      public void endPrefixMapping(String prefix) throws SAXException {
        delegado.endPrefixMapping(prefix);
      }

      @Override
      public void startElement(String uri, String localName, String qName, Attributes attributes)
          throws SAXException {
        if ("dimension".equals(localName)) {
          long declaradas = RangoUsadoExcel.filasDeclaradas(attributes.getValue("ref"));
          filasDeclaradas.set(declaradas);
          if (RangoUsadoExcel.excedeCeiling(declaradas)) {
            throw ArchivoRangoUsadoInvalidoException.porRangoDesproporcionado(
                declaradas, ArchivoRangoUsadoInvalidoException.FILAS_POBLADAS_DESCONOCIDAS);
          }
        }
        delegado.startElement(uri, localName, qName, attributes);
      }

      @Override
      public void endElement(String uri, String localName, String qName) throws SAXException {
        delegado.endElement(uri, localName, qName);
      }

      @Override
      public void characters(char[] ch, int start, int length) throws SAXException {
        delegado.characters(ch, start, length);
      }

      @Override
      public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        delegado.ignorableWhitespace(ch, start, length);
      }

      @Override
      public void processingInstruction(String target, String data) throws SAXException {
        delegado.processingInstruction(target, data);
      }

      @Override
      public void skippedEntity(String name) throws SAXException {
        delegado.skippedEntity(name);
      }
    };
  }

  private List<String[]> leerArchivoExcelXLS(byte[] contenido) throws IOException {
    List<String[]> filas = new ArrayList<>();

    try (HSSFWorkbook workbook = new HSSFWorkbook(new ByteArrayInputStream(contenido))) {
      Sheet sheet = workbook.getSheetAt(0); // Primera hoja

      for (Row row : sheet) {
        if (esFilaVacia(row)) {
          continue;
        }

        String[] campos = new String[row.getLastCellNum()];
        for (int i = 0; i < row.getLastCellNum(); i++) {
          Cell cell = row.getCell(i);
          campos[i] = obtenerValorCelda(cell);
        }
        filas.add(campos);
      }
    }

    logger.info("Se leyeron {} filas del archivo Excel XLS", filas.size());
    return filas;
  }

  private boolean esFilaVacia(Row row) {
    if (row == null) return true;

    for (Cell cell : row) {
      if (cell != null && cell.getCellType() != CellType.BLANK) {
        String valor = obtenerValorCelda(cell);
        if (valor != null && !valor.trim().isEmpty()) {
          return false;
        }
      }
    }
    return true;
  }

  private String obtenerValorCelda(Cell cell) {
    if (cell == null) {
      return "";
    }

    switch (cell.getCellType()) {
      case STRING:
        return cell.getStringCellValue();
      case NUMERIC:
        if (DateUtil.isCellDateFormatted(cell)) {
          return cell.getDateCellValue().toString();
        } else {
          // Convertir número a string sin notación científica
          double numValue = cell.getNumericCellValue();
          if (numValue == Math.floor(numValue)) {
            return String.valueOf((long) numValue);
          } else {
            return String.valueOf(numValue);
          }
        }
      case BOOLEAN:
        return String.valueOf(cell.getBooleanCellValue());
      case FORMULA:
        return cell.getCellFormula();
      case BLANK:
      default:
        return "";
    }
  }

  private String obtenerExtension(String nombreArchivo) {
    if (nombreArchivo == null || !nombreArchivo.contains(".")) {
      return "";
    }
    return nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1);
  }
}
