package conectaseguros.co.news_service.archivo.infrastructure.output.storage;

import conectaseguros.co.news_service.archivo.domain.exceptions.ArchivoException;
import conectaseguros.co.news_service.archivo.domain.model.enums.TipoArchivo;
import conectaseguros.co.news_service.archivo.domain.port.output.AlmacenadorArchivos;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocalFileStorageAdapter implements AlmacenadorArchivos {

  private static final Logger logger = LoggerFactory.getLogger(LocalFileStorageAdapter.class);
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

  private final String directorioBase;

  public LocalFileStorageAdapter(
      @Value("${app.storage.directorio-base:/var/conecta-seguros/archivos}")
          String directorioBase) {
    this.directorioBase = directorioBase;
    inicializarDirectorios();
  }

  @Override
  public String almacenar(
      byte[] contenido, String nombreArchivo, TipoArchivo tipo, String periodo) {
    try {
      String rutaCompleta = construirRutaArchivo(nombreArchivo, tipo, periodo);
      Path path = Paths.get(rutaCompleta);

      // Crear directorios padre si no existen
      Files.createDirectories(path.getParent());

      // Escribir el archivo
      Files.write(path, contenido, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

      logger.info("Archivo almacenado exitosamente en: {}", rutaCompleta);
      return rutaCompleta;

    } catch (IOException e) {
      logger.error("Error al almacenar archivo: {}", nombreArchivo, e);
      throw new ArchivoException("Error al almacenar archivo en el sistema de archivos", e);
    }
  }

  @Override
  public byte[] recuperar(String rutaArchivo) {
    try {
      Path path = Paths.get(rutaArchivo);

      if (!Files.exists(path)) {
        throw new ArchivoException("El archivo no existe en la ruta: " + rutaArchivo);
      }

      byte[] contenido = Files.readAllBytes(path);
      logger.debug("Archivo recuperado exitosamente desde: {}", rutaArchivo);
      return contenido;

    } catch (IOException e) {
      logger.error("Error al recuperar archivo desde: {}", rutaArchivo, e);
      throw new ArchivoException("Error al recuperar archivo del sistema de archivos", e);
    }
  }

  @Override
  public void eliminar(String rutaArchivo) {
    try {
      Path path = Paths.get(rutaArchivo);

      if (Files.exists(path)) {
        Files.delete(path);
        logger.info("Archivo eliminado exitosamente: {}", rutaArchivo);
      } else {
        logger.warn("Intento de eliminar archivo inexistente: {}", rutaArchivo);
      }

    } catch (IOException e) {
      logger.error("Error al eliminar archivo: {}", rutaArchivo, e);
      throw new ArchivoException("Error al eliminar archivo del sistema de archivos", e);
    }
  }

  @Override
  public boolean existe(String rutaArchivo) {
    Path path = Paths.get(rutaArchivo);
    boolean existe = Files.exists(path);
    logger.debug("Verificación de existencia para {}: {}", rutaArchivo, existe);
    return existe;
  }

  private void inicializarDirectorios() {
    try {
      // Crear directorio base si no existe
      Path directorioBasePath = Paths.get(directorioBase);
      Files.createDirectories(directorioBasePath);

      // Crear subdirectorios para cada tipo de archivo
      for (TipoArchivo tipo : TipoArchivo.values()) {
        Path subdirectorio = directorioBasePath.resolve(tipo.getValor().toLowerCase());
        Files.createDirectories(subdirectorio);
      }

      logger.info("Directorios de almacenamiento inicializados en: {}", directorioBase);

    } catch (IOException e) {
      logger.error("Error al inicializar directorios de almacenamiento", e);
      throw new ArchivoException("Error al inicializar sistema de almacenamiento", e);
    }
  }

  private String construirRutaArchivo(String nombreArchivo, TipoArchivo tipo, String periodo) {
    // Estructura: /directorio-base/tipo/periodo/timestamp_nombreArchivo
    String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
    String nombreConTimestamp = timestamp + "_" + nombreArchivo;

    return Paths.get(directorioBase)
        .resolve(tipo.getValor().toLowerCase())
        .resolve(periodo)
        .resolve(nombreConTimestamp)
        .toString();
  }
}
