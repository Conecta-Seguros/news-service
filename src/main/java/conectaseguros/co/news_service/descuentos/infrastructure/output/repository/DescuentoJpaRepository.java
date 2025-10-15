package conectaseguros.co.news_service.descuentos.infrastructure.output.repository;

import conectaseguros.co.news_service.descuentos.infrastructure.output.entity.DescuentoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DescuentoJpaRepository extends JpaRepository<DescuentoEntity, Long> {

    /**
     * Busca descuentos por archivo
     */
    List<DescuentoEntity> findByArchivoIdOrderByIdAsc(Long archivoId);

    /**
     * Busca descuentos por archivo con paginación
     */
    Page<DescuentoEntity> findByArchivoId(Long archivoId, Pageable pageable);

    /**
     * Verifica si existe un descuento para el cliente en el período y concepto específico
     */
    boolean existsByCedulaClienteAndPeriodoAndCodigoDescuento(
            String cedulaCliente, String periodo, String codigoDescuento);

    /**
     * Busca descuentos por cédula de cliente
     */
    List<DescuentoEntity> findByCedulaClienteOrderByPeriodoDesc(String cedulaCliente);

    /**
     * Busca descuentos por cédula y período
     */
    List<DescuentoEntity> findByCedulaClienteAndPeriodo(String cedulaCliente, String periodo);

    /**
     * Busca descuentos por seccional y período
     */
    @Query("SELECT d FROM DescuentoEntity d WHERE d.seccionalId = :seccionalId AND d.periodo = :periodo ORDER BY d.fechaCreacion DESC")
    List<DescuentoEntity> findBySeccionalAndPeriodo(@Param("seccionalId") Integer seccionalId,
                                                    @Param("periodo") String periodo);

    /**
     * Busca descuentos por seccional y período con paginación
     */
    @Query("SELECT d FROM DescuentoEntity d WHERE d.seccionalId = :seccionalId AND d.periodo = :periodo ORDER BY d.fechaCreacion DESC")
    Page<DescuentoEntity> findBySeccionalAndPeriodo(@Param("seccionalId") Integer seccionalId,
                                                    @Param("periodo") String periodo,
                                                    Pageable pageable);

    /**
     * Cuenta descuentos por seccional y período
     */
    @Query("SELECT COUNT(d) FROM DescuentoEntity d WHERE d.seccionalId = :seccionalId AND d.periodo = :periodo")
    long countBySeccionalAndPeriodo(@Param("seccionalId") Integer seccionalId,
                                   @Param("periodo") String periodo);

    /**
     * Cuenta descuentos por archivo
     */
    long countByArchivoId(Long archivoId);

    /**
     * Cuenta descuentos exitosos por archivo
     */
    @Query("SELECT COUNT(d) FROM DescuentoEntity d WHERE d.archivoId = :archivoId AND d.estado = 'PROCESADO'")
    long countByArchivoIdAndEstadoProcesado(@Param("archivoId") Long archivoId);

    /**
     * Busca descuentos por estado
     */
    List<DescuentoEntity> findByEstadoOrderByFechaCreacionDesc(String estado);

    /**
     * Obtiene estadísticas por archivo
     */
    @Query("""
        SELECT 
            COUNT(d) as total,
            SUM(CASE WHEN d.estado = 'PROCESADO' THEN 1 ELSE 0 END) as procesados,
            SUM(CASE WHEN d.estado = 'ERROR' THEN 1 ELSE 0 END) as errores,
            SUM(d.valor) as totalValor
        FROM DescuentoEntity d 
        WHERE d.archivoId = :archivoId
        """)
    Object[] getEstadisticasByArchivo(@Param("archivoId") Long archivoId);
}

