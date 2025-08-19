package conectaseguros.co.news_service.archivo.infrastructure.output.repository;

import conectaseguros.co.news_service.archivo.infrastructure.output.entity.ArchivoNovedadesEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaArchivoNovedadesRepository extends JpaRepository<ArchivoNovedadesEntity, Long> {

    Optional<ArchivoNovedadesEntity> findByNombreArchivoAndPeriodo(String nombreArchivo, String periodo);

    @Query("SELECT a FROM ArchivoNovedadesEntity a WHERE a.periodo = :periodo AND a.seccionalId = :seccionalId")
    List<ArchivoNovedadesEntity> findByPeriodoAndSeccionalId(@Param("periodo") String periodo,
                                                             @Param("seccionalId") Integer seccionalId);

    @Query("SELECT a FROM ArchivoNovedadesEntity a WHERE a.estado = :estado")
    List<ArchivoNovedadesEntity> findByEstado(@Param("estado") String estado);

    @Query("SELECT a FROM ArchivoNovedadesEntity a WHERE a.usuarioCarga = :usuarioId ORDER BY a.fechaCarga DESC")
    List<ArchivoNovedadesEntity> findByUsuarioCargaOrderByFechaCargaDesc(@Param("usuarioId") Integer usuarioId);

    @Query("SELECT COUNT(a) FROM ArchivoNovedadesEntity a WHERE a.periodo = :periodo AND a.estado = 'PROCESANDO'")
    Long countByPeriodoAndEstadoProcesando(@Param("periodo") String periodo);
}
