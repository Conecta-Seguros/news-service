package conectaseguros.co.news_service.descuentos.domain.port.output;

import conectaseguros.co.news_service.descuentos.domain.model.DescuentoProcesado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Puerto de salida para consultas de descuentos procesados
 */
public interface ConsultaDescuentosRepository {

    /**
     * Busca descuentos por periodo y seccional con paginación
     */
    Page<DescuentoProcesado> buscarPorPeriodoYSeccional(String periodo, Integer seccionalId, Pageable pageable);

    /**
     * Busca descuentos por periodo y seccional sin paginación
     */
    List<DescuentoProcesado> buscarPorPeriodoYSeccional(String periodo, Integer seccionalId);

    /**
     * Busca descuentos por cédula de cliente
     */
    List<DescuentoProcesado> buscarPorCedulaCliente(String cedulaCliente);

    /**
     * Cuenta los descuentos por periodo y seccional
     */
    long contarPorPeriodoYSeccional(String periodo, Integer seccionalId);
}
