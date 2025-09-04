package conectaseguros.co.news_service.repository;

import conectaseguros.co.news_service.model.ProductoCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductoClienteRepository extends JpaRepository<ProductoCliente, Long> {
    List<ProductoCliente> findByClienteId(Long clienteId);
    List<ProductoCliente> findByAseguradoId(Long aseguradoId);
    List<ProductoCliente> findByClienteIdAndEstado(Long clienteId, String estado);
    boolean existsByClienteIdAndPolizaIdAndAseguradoId(Long clienteId, Long polizaId, Long aseguradoId);

    @Query("SELECT pc FROM ProductoCliente pc JOIN FETCH pc.poliza p JOIN FETCH p.producto WHERE pc.clienteId = :clienteId")
    List<ProductoCliente> findByClienteIdWithDetails(@Param("clienteId") Long clienteId);

    // ... otros métodos existentes
}
