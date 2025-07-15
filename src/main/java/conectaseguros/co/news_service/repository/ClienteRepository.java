package conectaseguros.co.news_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByCedula(String cedula);
    Optional<Cliente> findByCedula(String cedula);
    // ... otros métodos existentes
}