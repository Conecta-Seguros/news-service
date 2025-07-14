package conectaseguros.co.news_service.repository;

import conectaseguros.co.news_service.model.ArchivoNovedades;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ArchivoNovedadesRepository extends JpaRepository<ArchivoNovedades, Long> {
    List<ArchivoNovedades> findBySeccionalId(Long seccionalId);
    List<ArchivoNovedades> findByPeriodo(String periodo);
    List<ArchivoNovedades> findByEstado(String estado);
}
