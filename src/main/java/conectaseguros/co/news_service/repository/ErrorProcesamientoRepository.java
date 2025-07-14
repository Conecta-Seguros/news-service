package conectaseguros.co.news_service.repository;

import conectaseguros.co.news_service.model.ErrorProcesamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ErrorProcesamientoRepository extends JpaRepository<ErrorProcesamiento, Long> {
    List<ErrorProcesamiento> findByArchivoId(Long archivoId);
    List<ErrorProcesamiento> findByTipoArchivo(String tipoArchivo);
    List<ErrorProcesamiento> findByCedulaCliente(String cedulaCliente);
}
