package conectaseguros.co.news_service.repository;

import conectaseguros.co.news_service.model.Novedad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface NovedadRepository extends JpaRepository<Novedad, Long> {
    List<Novedad> findByClienteId(Long clienteId);
    List<Novedad> findByCedulaCliente(String cedulaCliente);
    List<Novedad> findByNumeroPoliza(String numeroPoliza);
    List<Novedad> findByEstado(String estado);
    List<Novedad> findByTipoNovedad(String tipoNovedad);
    List<Novedad> findByArchivoOrigen(String archivoOrigen);

    @Query("SELECT n FROM Novedad n WHERE n.fechaAplicacion BETWEEN :fechaInicio AND :fechaFin")
    List<Novedad> findByRangoFechas(@Param("fechaInicio") LocalDate fechaInicio,
                                    @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT n FROM Novedad n WHERE n.cedulaCliente = :cedula AND n.estado = 'ACTIVO'")
    List<Novedad> findNovedadesActivasPorCliente(@Param("cedula") String cedula);

    boolean existsByCedulaClienteAndNumeroPolizaAndTipoNovedad(String cedulaCliente, String numeroPoliza, String tipoNovedad);
}
