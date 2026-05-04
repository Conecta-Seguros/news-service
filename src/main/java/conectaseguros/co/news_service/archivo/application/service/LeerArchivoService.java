package conectaseguros.co.news_service.archivo.application.service;

import conectaseguros.co.news_service.archivo.domain.exceptions.ArchivoNoEncontradoException;
import conectaseguros.co.news_service.archivo.domain.exceptions.ArchivoNoValidoException;
import conectaseguros.co.news_service.archivo.domain.model.ArchivoDescuentos;
import conectaseguros.co.news_service.archivo.domain.model.ArchivoNovedades;
import conectaseguros.co.news_service.archivo.domain.model.enums.TipoArchivo;
import conectaseguros.co.news_service.archivo.domain.port.input.LeerArchivoUseCase;
import conectaseguros.co.news_service.archivo.domain.port.output.AlmacenadorArchivos;
import conectaseguros.co.news_service.archivo.domain.port.output.ArchivoDescuentosRepository;
import conectaseguros.co.news_service.archivo.domain.port.output.ArchivoNovedadesRepository;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
          return leerArchivoExcelXLSX(contenido);
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

  private List<String[]> leerArchivoExcelXLSX(byte[] contenido) throws IOException {
    List<String[]> filas = new ArrayList<>();

    try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(contenido))) {
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

    logger.info("Se leyeron {} filas del archivo Excel XLSX", filas.size());
    return filas;
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
