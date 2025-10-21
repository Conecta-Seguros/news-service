package conectaseguros.co.news_service.descuentos.infrastructure.output.repository;

import conectaseguros.co.news_service.descuentos.infrastructure.output.entity.ErrorProcesamientoEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorProcesamientoJpaRepository
    extends JpaRepository<ErrorProcesamientoEntity, Long> {

  /** Busca errores por archivo */
  List<ErrorProcesamientoEntity> findByArchivoIdOrderByLineaArchivoAsc(Long archivoId);

  /** Busca errores por archivo con paginación */
  Page<ErrorProcesamientoEntity> findByArchivoIdOrderByLineaArchivoAsc(
      Long archivoId, Pageable pageable);

  /** Busca errores por tipo */
  List<ErrorProcesamientoEntity> findByArchivoIdAndTipoErrorOrderByLineaArchivoAsc(
      Long archivoId, String tipoError);

  /** Cuenta errores por archivo */
  long countByArchivoId(Long archivoId);

  /** Cuenta errores por tipo */
  long countByArchivoIdAndTipoError(Long archivoId, String tipoError);

  /** Busca errores por cliente */
  List<ErrorProcesamientoEntity> findByCedulaClienteOrderByFechaErrorDesc(String cedulaCliente);

  /** Busca errores en un rango de fechas */
  @Query(
      "SELECT e FROM ErrorProcesamientoEntity e WHERE e.archivoId = :archivoId "
          + "AND e.fechaError BETWEEN :fechaInicio AND :fechaFin ORDER BY e.fechaError DESC")
  List<ErrorProcesamientoEntity> findByArchivoIdAndFechaRange(
      @Param("archivoId") Long archivoId,
      @Param("fechaInicio") LocalDateTime fechaInicio,
      @Param("fechaFin") LocalDateTime fechaFin);

  /** Obtiene estadísticas de errores por tipo */
  @Query(
      "SELECT e.tipoError, COUNT(e) FROM ErrorProcesamientoEntity e "
          + "WHERE e.archivoId = :archivoId GROUP BY e.tipoError ORDER BY COUNT(e) DESC")
  List<Object[]> getEstadisticasErroresPorTipo(@Param("archivoId") Long archivoId);

  /** Obtiene top errores más frecuentes */
  @Query(
      "SELECT e.descripcionError, COUNT(e) FROM ErrorProcesamientoEntity e "
          + "WHERE e.archivoId = :archivoId GROUP BY e.descripcionError "
          + "ORDER BY COUNT(e) DESC")
  List<Object[]> getTopErroresFrecuentes(@Param("archivoId") Long archivoId, Pageable pageable);

  /** Cuenta clientes únicos con errores */
  @Query(
      "SELECT COUNT(DISTINCT e.cedulaCliente) FROM ErrorProcesamientoEntity e "
          + "WHERE e.archivoId = :archivoId AND e.cedulaCliente IS NOT NULL")
  long countClientesUnicosConErrores(@Param("archivoId") Long archivoId);

  /** Obtiene los IDs de archivos únicos que tienen errores */
  @Query("SELECT DISTINCT e.archivoId FROM ErrorProcesamientoEntity e ORDER BY e.archivoId")
  List<Long> findDistinctArchivoIdsWithErrors();
}
