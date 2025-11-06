package conectaseguros.co.news_service.descuentos.application.service;

import conectaseguros.co.news_service.archivo.domain.exceptions.ArchivoNoEncontradoException;
import conectaseguros.co.news_service.archivo.domain.model.ArchivoDescuentos;
import conectaseguros.co.news_service.archivo.domain.model.enums.EstadoArchivo;
import conectaseguros.co.news_service.archivo.domain.port.output.ArchivoDescuentosRepository;
import conectaseguros.co.news_service.descuentos.domain.model.ErrorProcesamiento;
import conectaseguros.co.news_service.descuentos.domain.model.EstadoProcesamiento;
import conectaseguros.co.news_service.descuentos.domain.port.output.ErrorProcesamientoRepository;
import conectaseguros.co.news_service.descuentos.infrastructure.input.dto.ArchivoConErroresDTO;
import conectaseguros.co.news_service.descuentos.infrastructure.input.dto.EstadisticasProcesamientoDTO;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ConsultaProcesamientoService {

  private static final Logger logger = LoggerFactory.getLogger(ConsultaProcesamientoService.class);

  private final ArchivoDescuentosRepository archivoRepository;
  private final ErrorProcesamientoRepository errorRepository;

  public ConsultaProcesamientoService(
      ArchivoDescuentosRepository archivoRepository, ErrorProcesamientoRepository errorRepository) {
    this.archivoRepository = archivoRepository;
    this.errorRepository = errorRepository;
  }

  /** Obtiene el estado actual del procesamiento de un archivo */
  public EstadoProcesamiento obtenerEstadoProcesamiento(Long archivoId) {
    logger.debug("Consultando estado de procesamiento para archivo: {}", archivoId);

    ArchivoDescuentos archivo =
        archivoRepository
            .buscarPorId(archivoId)
            .orElseThrow(
                () -> new ArchivoNoEncontradoException("Archivo no encontrado: " + archivoId));

    return new EstadoProcesamiento(
        archivo.getEstado(),
        archivo.getTotalRegistros() != null ? archivo.getTotalRegistros() : 0,
        archivo.getRegistrosProcesados() != null ? archivo.getRegistrosProcesados() : 0,
        calcularRegistrosExitosos(archivo),
        archivo.getDiferenciasEncontradas() != null ? archivo.getDiferenciasEncontradas() : 0,
        archivo.getExcedentes() != null ? archivo.getExcedentes() : 0,
        archivo.getFaltantes() != null ? archivo.getFaltantes() : 0,
        archivo.getNoLlegaron() != null ? archivo.getNoLlegaron() : 0,
        archivo.getFechaCarga(),
        null, // fecha fin no está en el modelo actual
        obtenerMensajeEstado(archivo));
  }

  /** Obtiene la lista de errores de procesamiento de un archivo */
  public List<ErrorProcesamiento> obtenerErroresProcesamiento(
      Long archivoId, int limite, int offset) {
    logger.debug(
        "Consultando errores de procesamiento para archivo: {} (límite: {}, offset: {})",
        archivoId,
        limite,
        offset);

    // Verificar que el archivo existe
    archivoRepository
        .buscarPorId(archivoId)
        .orElseThrow(() -> new ArchivoNoEncontradoException("Archivo no encontrado: " + archivoId));

    return errorRepository.buscarPorArchivo(archivoId);
  }

  /** Genera estadísticas detalladas del procesamiento */
  public EstadisticasProcesamientoDTO generarEstadisticas(Long archivoId) {
    logger.debug("Generando estadísticas para archivo: {}", archivoId);

    ArchivoDescuentos archivo =
        archivoRepository
            .buscarPorId(archivoId)
            .orElseThrow(
                () -> new ArchivoNoEncontradoException("Archivo no encontrado: " + archivoId));

    List<ErrorProcesamiento> errores = errorRepository.buscarPorArchivo(archivoId);

    EstadisticasProcesamientoDTO estadisticas = new EstadisticasProcesamientoDTO();
    estadisticas.setArchivoId(archivoId);

    // Resumen general
    int totalLineas = archivo.getTotalRegistros() != null ? archivo.getTotalRegistros() : 0;
    int conErrores =
        archivo.getDiferenciasEncontradas() != null ? archivo.getDiferenciasEncontradas() : 0;
    int exitosas = calcularRegistrosExitosos(archivo);

    estadisticas.setResumen(
        new EstadisticasProcesamientoDTO.ResumenDTO(totalLineas, exitosas, conErrores));

    // Errores por tipo
    Map<String, Integer> erroresPorTipo =
        errores.stream()
            .collect(
                Collectors.groupingBy(
                    error -> error.getTipoError().name(),
                    Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)));
    estadisticas.setErroresPorTipo(erroresPorTipo);

    // Top errores más frecuentes (por descripción)
    Map<String, Integer> topErrores =
        errores.stream()
            .collect(
                Collectors.groupingBy(
                    ErrorProcesamiento::getDescripcionError,
                    Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)))
            .entrySet()
            .stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    java.util.LinkedHashMap::new));
    estadisticas.setTopErroresFrecuentes(topErrores);

    // Clientes únicos con errores
    long clientesConErrores =
        errores.stream()
            .filter(error -> error.getCedulaCliente() != null)
            .map(ErrorProcesamiento::getCedulaCliente)
            .distinct()
            .count();
    estadisticas.setClientesConErrores((int) clientesConErrores);

    // Tiempo de procesamiento (estimado)
    estadisticas.setTiempoProcesamiento(calcularTiempoProcesamiento(archivo));

    return estadisticas;
  }

  /** Verifica si un archivo está siendo procesado actualmente */
  public boolean estaEnProcesamiento(Long archivoId) {
    boolean resultado =
        archivoRepository
            .buscarPorId(archivoId)
            .map(
                archivo -> {
                  boolean procesando = archivo.getEstado() == EstadoArchivo.PROCESANDO;
                  logger.debug(
                      "Archivo {} - Estado: {}, En procesamiento: {}",
                      archivoId,
                      archivo.getEstado(),
                      procesando);
                  return procesando;
                })
            .orElse(false);

    logger.info("¿Archivo {} está en procesamiento? {}", archivoId, resultado);
    return resultado;
  }

  /** Obtiene el progreso de procesamiento como porcentaje */
  public double obtenerPorcentajeProgreso(Long archivoId) {
    return archivoRepository
        .buscarPorId(archivoId)
        .map(
            archivo -> {
              int total = archivo.getTotalRegistros() != null ? archivo.getTotalRegistros() : 0;
              int procesados =
                  archivo.getRegistrosProcesados() != null ? archivo.getRegistrosProcesados() : 0;
              return total > 0 ? (double) procesados / total * 100.0 : 0.0;
            })
        .orElse(0.0);
  }

  private int calcularRegistrosExitosos(ArchivoDescuentos archivo) {
    int procesados =
        archivo.getRegistrosProcesados() != null ? archivo.getRegistrosProcesados() : 0;
    int errores =
        archivo.getDiferenciasEncontradas() != null ? archivo.getDiferenciasEncontradas() : 0;
    return procesados - errores;
  }

  private String obtenerMensajeEstado(ArchivoDescuentos archivo) {
    return switch (archivo.getEstado()) {
      case CARGADO -> "Archivo cargado, listo para procesar";
      case PROCESANDO ->
          String.format(
              "Procesando... %d de %d líneas completadas",
              archivo.getRegistrosProcesados(), archivo.getTotalRegistros());
      case PROCESADO ->
          String.format(
              "Procesamiento completado. %d exitosas, %d errores",
              calcularRegistrosExitosos(archivo), archivo.getDiferenciasEncontradas());
      case ERROR -> "Error durante el procesamiento";
    };
  }

  private Long calcularTiempoProcesamiento(ArchivoDescuentos archivo) {
    // Estimación basada en la fecha de carga
    // En una implementación real, guardarías fecha de inicio y fin del procesamiento
    return Duration.between(archivo.getFechaCarga(), java.time.LocalDateTime.now()).getSeconds();
  }

  /** Obtiene la lista de archivos que tienen errores */
  public List<ArchivoConErroresDTO> obtenerArchivosConErrores() {
    logger.debug("Consultando archivos con errores");

    // Obtener los IDs de archivos únicos que tienen errores
    List<Long> archivoIds = errorRepository.findDistinctArchivoIdsWithErrors();

    // Para cada archivo, obtener su información y el total de errores
    return archivoIds.stream()
        .map(
            archivoId -> {
              // Obtener información del archivo
              ArchivoDescuentos archivo = archivoRepository.buscarPorId(archivoId).orElse(null);

              if (archivo != null) {
                // Contar errores para este archivo
                long totalErrores = errorRepository.contarErroresPorArchivo(archivoId);

                // Crear DTO
                return new ArchivoConErroresDTO(
                    archivo.getId(),
                    archivo.getNombreArchivo(),
                    archivo.getPeriodo(),
                    archivo.getSeccionalId(),
                    totalErrores,
                    archivo.getEstado() != null ? archivo.getEstado().name() : null);
              }

              return null;
            })
        .filter(java.util.Objects::nonNull)
        .toList();
  }
}
