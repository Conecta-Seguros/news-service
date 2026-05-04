package conectaseguros.co.news_service.archivo.infrastructure.output.repository;

import conectaseguros.co.news_service.archivo.infrastructure.output.entity.ArchivoDescuentosEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaArchivoDescuentosRepository
    extends JpaRepository<ArchivoDescuentosEntity, Long> {

  Optional<ArchivoDescuentosEntity> findByNombreArchivoAndPeriodo(
      String nombreArchivo, String periodo);

  @Query(
      "SELECT a FROM ArchivoDescuentosEntity a WHERE a.periodo = :periodo AND a.seccionalId = :seccionalId")
  List<ArchivoDescuentosEntity> findByPeriodoAndSeccionalId(
      @Param("periodo") String periodo, @Param("seccionalId") Integer seccionalId);

  @Query("SELECT a FROM ArchivoDescuentosEntity a WHERE a.estado = :estado")
  List<ArchivoDescuentosEntity> findByEstado(@Param("estado") String estado);

  @Query(
      "SELECT a FROM ArchivoDescuentosEntity a WHERE a.usuarioCarga = :usuarioId ORDER BY a.fechaCarga DESC")
  List<ArchivoDescuentosEntity> findByUsuarioCargaOrderByFechaCargaDesc(
      @Param("usuarioId") Integer usuarioId);

  @Query(
      "SELECT COUNT(a) FROM ArchivoDescuentosEntity a WHERE a.periodo = :periodo AND a.estado = 'PROCESANDO'")
  Long countByPeriodoAndEstadoProcesando(@Param("periodo") String periodo);

  @Query(
      "SELECT new conectaseguros.co.news_service.archivo.infrastructure.output.entity.ArchivoDescuentosEntity("
          + "a.id, a.nombreArchivo, a.periodo, a.seccionalId, a.estado, a.totalRegistros,"
          + "a.registrosProcesados, a.diferenciasEncontradas, a.excedentes, a.faltantes,"
          + "a.noLlegaron, a.tamanoBytes, a.fechaCarga, a.usuarioCarga)"
          + " FROM ArchivoDescuentosEntity a ORDER BY a.fechaCarga DESC")
  Page<ArchivoDescuentosEntity> findAllSinContenido(Pageable pageable);
}
