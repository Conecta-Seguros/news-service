package conectaseguros.co.news_service.repository;

import conectaseguros.co.news_service.model.ProcesamientoArchivo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ProcesamientoArchivoRepository extends JpaRepository<ProcesamientoArchivo, Long> {
    List<ProcesamientoArchivo> findByEstado(String estado);
    List<ProcesamientoArchivo> findByUsuarioProcesamiento(String usuario);
    List<ProcesamientoArchivo> findByFechaInicioBetween(LocalDateTime inicio, LocalDateTime fin);
    boolean existsByNombreArchivo(String nombreArchivo);
    List<ProcesamientoArchivo> findTop10ByOrderByFechaInicioDesc();
}