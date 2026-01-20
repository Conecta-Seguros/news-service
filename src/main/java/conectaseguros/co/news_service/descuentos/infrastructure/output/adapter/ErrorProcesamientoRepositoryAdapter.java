package conectaseguros.co.news_service.descuentos.infrastructure.output.adapter;

import conectaseguros.co.news_service.descuentos.domain.model.ErrorProcesamiento;
import conectaseguros.co.news_service.descuentos.domain.model.enums.TipoError;
import conectaseguros.co.news_service.descuentos.domain.port.output.ErrorProcesamientoRepository;
import conectaseguros.co.news_service.descuentos.infrastructure.output.entity.ErrorProcesamientoEntity;
import conectaseguros.co.news_service.descuentos.infrastructure.output.mapper.ErrorProcesamientoMapper;
import conectaseguros.co.news_service.descuentos.infrastructure.output.repository.ErrorProcesamientoJpaRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ErrorProcesamientoRepositoryAdapter implements ErrorProcesamientoRepository {

  private static final Logger logger =
      LoggerFactory.getLogger(ErrorProcesamientoRepositoryAdapter.class);

  private final ErrorProcesamientoJpaRepository jpaRepository;
  private final ErrorProcesamientoMapper mapper;

  public ErrorProcesamientoRepositoryAdapter(
      ErrorProcesamientoJpaRepository jpaRepository, ErrorProcesamientoMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public ErrorProcesamiento guardar(ErrorProcesamiento error) {
    logger.debug("Guardando error de procesamiento: {}", error);

    try {
      ErrorProcesamientoEntity entity = mapper.toEntity(error);
      ErrorProcesamientoEntity savedEntity = jpaRepository.save(entity);
      ErrorProcesamiento resultado = mapper.toDomain(savedEntity);

      logger.debug("Error guardado con ID: {}", savedEntity.getId());
      return resultado;

    } catch (Exception e) {
      logger.error("Error al guardar error de procesamiento: {}", error, e);
      throw new RuntimeException("Error al guardar error de procesamiento", e);
    }
  }

  @Override
  public List<ErrorProcesamiento> guardarLote(List<ErrorProcesamiento> errores) {
    if (errores == null || errores.isEmpty()) {
      return List.of();
    }

    logger.debug("Guardando lote de {} errores", errores.size());

    try {
      List<ErrorProcesamientoEntity> entities = mapper.toEntityList(errores);
      List<ErrorProcesamientoEntity> savedEntities = jpaRepository.saveAll(entities);
      List<ErrorProcesamiento> resultado = mapper.toDomainList(savedEntities);

      logger.debug("Lote de {} errores guardado exitosamente", savedEntities.size());
      return resultado;

    } catch (Exception e) {
      logger.error("Error al guardar lote de {} errores", errores.size(), e);
      throw new RuntimeException("Error al guardar lote de errores", e);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<ErrorProcesamiento> buscarPorArchivo(Long archivoId) {
    logger.debug("Buscando errores por archivo: {}", archivoId);

    try {
      List<ErrorProcesamientoEntity> entities =
          jpaRepository.findByArchivoIdOrderByLineaArchivoAsc(archivoId);
      List<ErrorProcesamiento> resultado = mapper.toDomainList(entities);

      logger.debug("Encontrados {} errores para archivo {}", resultado.size(), archivoId);
      return resultado;

    } catch (Exception e) {
      logger.error("Error al buscar errores por archivo: {}", archivoId, e);
      throw new RuntimeException("Error al buscar errores por archivo", e);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<ErrorProcesamiento> buscarPorTipoError(Long archivoId, TipoError tipoError) {
    logger.debug("Buscando errores por archivo {} y tipo {}", archivoId, tipoError);

    try {
      List<ErrorProcesamientoEntity> entities =
          jpaRepository.findByArchivoIdAndTipoErrorOrderByLineaArchivoAsc(
              archivoId, tipoError.name());
      return mapper.toDomainList(entities);

    } catch (Exception e) {
      logger.error(
          "Error al buscar errores por tipo: archivo={}, tipo={}", archivoId, tipoError, e);
      throw new RuntimeException("Error al buscar errores por tipo", e);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public long contarErroresPorArchivo(Long archivoId) {
    try {
      return jpaRepository.countByArchivoId(archivoId);
    } catch (Exception e) {
      logger.error("Error al contar errores por archivo: {}", archivoId, e);
      return 0;
    }
  }

  /** Métodos adicionales para estadísticas */
  @Transactional(readOnly = true)
  public Map<String, Long> obtenerEstadisticasPorTipo(Long archivoId) {
    logger.debug("Obteniendo estadísticas de errores por tipo para archivo: {}", archivoId);

    try {
      List<Object[]> resultados = jpaRepository.getEstadisticasErroresPorTipo(archivoId);

      return resultados.stream()
          .collect(Collectors.toMap(result -> (String) result[0], result -> (Long) result[1]));

    } catch (Exception e) {
      logger.error("Error al obtener estadísticas por tipo para archivo: {}", archivoId, e);
      return Map.of();
    }
  }

  @Transactional(readOnly = true)
  public Map<String, Long> obtenerTopErroresFrecuentes(Long archivoId, int limite) {
    logger.debug("Obteniendo top {} errores frecuentes para archivo: {}", limite, archivoId);

    try {
      List<Object[]> resultados =
          jpaRepository.getTopErroresFrecuentes(archivoId, PageRequest.of(0, limite));

      return resultados.stream()
          .collect(
              Collectors.toMap(
                  result -> (String) result[0],
                  result -> (Long) result[1],
                  (v1, v2) -> v1,
                  java.util.LinkedHashMap::new));

    } catch (Exception e) {
      logger.error("Error al obtener top errores frecuentes para archivo: {}", archivoId, e);
      return Map.of();
    }
  }

  @Transactional(readOnly = true)
  public long contarClientesUnicosConErrores(Long archivoId) {
    try {
      return jpaRepository.countClientesUnicosConErrores(archivoId);
    } catch (Exception e) {
      logger.error("Error al contar clientes únicos con errores para archivo: {}", archivoId, e);
      return 0;
    }
  }

  @Transactional(readOnly = true)
  public List<ErrorProcesamiento> buscarPorCliente(String cedulaCliente) {
    logger.debug("Buscando errores por cliente: {}", cedulaCliente);

    try {
      List<ErrorProcesamientoEntity> entities =
          jpaRepository.findByCedulaClienteOrderByFechaErrorDesc(cedulaCliente);
      return mapper.toDomainList(entities);

    } catch (Exception e) {
      logger.error("Error al buscar errores por cliente: {}", cedulaCliente, e);
      throw new RuntimeException("Error al buscar errores por cliente", e);
    }
  }

  @Transactional(readOnly = true)
  public List<Long> findDistinctArchivoIdsWithErrors() {
    logger.debug("Buscando IDs de archivos únicos con errores");

    try {
      List<Long> archivoIds = jpaRepository.findDistinctArchivoIdsWithErrors();
      logger.debug("Encontrados {} archivos con errores", archivoIds.size());
      return archivoIds;

    } catch (Exception e) {
      logger.error("Error al buscar archivos con errores", e);
      throw new RuntimeException("Error al buscar archivos con errores", e);
    }
  }

  @Override
  public void eliminarPorArchivo(Long archivoId) {
    logger.debug("Eliminando errores del archivo: {}", archivoId);
    try {
      jpaRepository.deleteByArchivoId(archivoId);
      logger.debug("Errores eliminados exitosamente para el archivo: {}", archivoId);
    } catch (Exception e) {
      logger.error("Error al eliminar errores del archivo: {}", archivoId, e);
      throw new RuntimeException("Error al eliminar errores del archivo", e);
    }
  }
}
