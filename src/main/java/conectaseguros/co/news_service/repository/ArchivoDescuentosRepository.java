package conectaseguros.co.news_service.repository;

import conectaseguros.co.news_service.model.ArchivoDescuentos;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ArchivoDescuentosRepository extends JpaRepository<ArchivoDescuentos, Long> {
    List<ArchivoDescuentos> findBySeccionalId(Long seccionalId);
    List<ArchivoDescuentos> findByPeriodo(String periodo);
    List<ArchivoDescuentos> findByEstado(String estado);
}
