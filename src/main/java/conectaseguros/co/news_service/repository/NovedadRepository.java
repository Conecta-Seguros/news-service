package conectaseguros.co.news_service.repository;

import conectaseguros.co.news_service.model.Novedad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NovedadRepository extends JpaRepository<Novedad, Long> {
    List<Novedad> findByClienteId(Long clienteId);
    List<Novedad> findByPeriodo(String periodo);
    List<Novedad> findByEstado(String estado);
    List<Novedad> findByArchivoId(Long archivoId);
}
