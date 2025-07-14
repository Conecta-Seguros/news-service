package conectaseguros.co.news_service.repository;

import conectaseguros.co.news_service.model.Descuento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DescuentoRepository extends JpaRepository<Descuento, Long> {
    List<Descuento> findByClienteId(Long clienteId);
    List<Descuento> findBySeccionalId(Long seccionalId);
    List<Descuento> findByEstado(String estado);
    List<Descuento> findByArchivoId(Long archivoId);
}
