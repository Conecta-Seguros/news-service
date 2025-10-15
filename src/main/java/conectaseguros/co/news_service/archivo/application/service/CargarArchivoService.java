package conectaseguros.co.news_service.archivo.application.service;

import conectaseguros.co.news_service.archivo.domain.exceptions.ArchivoNoValidoException;
import conectaseguros.co.news_service.archivo.domain.model.ArchivoDescuentos;
import conectaseguros.co.news_service.archivo.domain.model.ArchivoNovedades;
import conectaseguros.co.news_service.archivo.domain.model.enums.TipoArchivo;
import conectaseguros.co.news_service.archivo.domain.model.valueobjects.MetadatosArchivo;
import conectaseguros.co.news_service.archivo.domain.port.input.CargarArchivoUseCase;
import conectaseguros.co.news_service.archivo.domain.port.output.AlmacenadorArchivos;
import conectaseguros.co.news_service.archivo.domain.port.output.ArchivoDescuentosRepository;
import conectaseguros.co.news_service.archivo.domain.port.output.ArchivoNovedadesRepository;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CargarArchivoService implements CargarArchivoUseCase {

    private static final Logger logger = LoggerFactory.getLogger(CargarArchivoService.class);

    private static final List<String> TIPOS_MIME_PERMITIDOS = Arrays.asList(
            "text/csv",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain"
    );

    private static final long TAMANO_MAXIMO_BYTES = 50 * 1024 * 1024; // 50MB

    private final ArchivoNovedadesRepository archivoNovedadesRepository;
    private final ArchivoDescuentosRepository archivoDescuentosRepository;
    private final AlmacenadorArchivos almacenadorArchivos;

    public CargarArchivoService(ArchivoNovedadesRepository archivoNovedadesRepository,
                                ArchivoDescuentosRepository archivoDescuentosRepository,
                                AlmacenadorArchivos almacenadorArchivos) {
        this.archivoNovedadesRepository = archivoNovedadesRepository;
        this.archivoDescuentosRepository = archivoDescuentosRepository;
        this.almacenadorArchivos = almacenadorArchivos;
    }

    @Override
    @Transactional
    public Long cargarArchivo(MultipartFile archivo, TipoArchivo tipoArchivo,
                              String periodo, Integer seccionalId, Integer usuarioId) {

        logger.info("Iniciando carga de archivo: {} de tipo: {} para periodo: {}",
                archivo.getOriginalFilename(), tipoArchivo, periodo);

        validarArchivo(archivo);

        try {
            byte[] contenido = archivo.getBytes();
            String checksum = calcularChecksum(contenido);

            MetadatosArchivo metadatos = new MetadatosArchivo(
                    archivo.getOriginalFilename(),
                    archivo.getContentType(),
                    archivo.getSize(),
                    checksum,
                    LocalDateTime.now(),
                    usuarioId
            );

            // Almacenar físicamente el archivo
            String rutaArchivo = almacenadorArchivos.almacenar(
                    contenido,
                    archivo.getOriginalFilename(),
                    tipoArchivo,
                    periodo
            );

            // Guardar en base de datos según el tipo
            Long archivoId = guardarSegunTipo(tipoArchivo, archivo.getOriginalFilename(),
                    periodo, seccionalId, rutaArchivo, contenido, metadatos);

            logger.info("Archivo cargado exitosamente con ID: {}", archivoId);
            return archivoId;

        } catch (IOException e) {
            logger.error("Error al leer el archivo: {}", archivo.getOriginalFilename(), e);
            throw new ArchivoNoValidoException("Error al leer el archivo: " + e.getMessage());
        }
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ArchivoNoValidoException("El archivo no puede estar vacío");
        }

        if (archivo.getSize() > TAMANO_MAXIMO_BYTES) {
            throw new ArchivoNoValidoException(
                    String.format("El archivo excede el tamaño máximo permitido de %d MB",
                            TAMANO_MAXIMO_BYTES / (1024 * 1024))
            );
        }

        String tipoMime = archivo.getContentType();
        if (tipoMime == null || !TIPOS_MIME_PERMITIDOS.contains(tipoMime.toLowerCase())) {
            throw new ArchivoNoValidoException(
                    "Tipo de archivo no válido. Formatos permitidos: CSV, Excel (.xls, .xlsx), TXT"
            );
        }

        String nombreArchivo = archivo.getOriginalFilename();
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            throw new ArchivoNoValidoException("El archivo debe tener un nombre válido");
        }
    }

    private String calcularChecksum(byte[] contenido) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(contenido);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error al calcular checksum SHA-256", e);
            throw new RuntimeException("Error al calcular checksum del archivo", e);
        }
    }

    private Long guardarSegunTipo(TipoArchivo tipoArchivo, String nombreArchivo, String periodo,
                                  Integer seccionalId, String rutaArchivo, byte[] contenido,
                                  MetadatosArchivo metadatos) {

        switch (tipoArchivo) {
            case NOVEDADES:
                ArchivoNovedades archivoNovedades = new ArchivoNovedades(
                        nombreArchivo, periodo, seccionalId, rutaArchivo, contenido, metadatos
                );
                ArchivoNovedades savedNovedades = archivoNovedadesRepository.guardar(archivoNovedades);
                return savedNovedades.getId();

            case DESCUENTOS:
                ArchivoDescuentos archivoDescuentos = new ArchivoDescuentos(
                        nombreArchivo, periodo, seccionalId, rutaArchivo, contenido, metadatos
                );
                ArchivoDescuentos savedDescuentos = archivoDescuentosRepository.guardar(archivoDescuentos);
                return savedDescuentos.getId();

            default:
                throw new ArchivoNoValidoException("Tipo de archivo no soportado: " + tipoArchivo);
        }
    }
}