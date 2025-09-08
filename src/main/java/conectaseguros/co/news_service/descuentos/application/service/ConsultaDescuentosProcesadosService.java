package conectaseguros.co.news_service.descuentos.application.service;

import conectaseguros.co.news_service.descuentos.domain.model.DescuentoProcesado;
import conectaseguros.co.news_service.descuentos.domain.port.output.ConsultaDescuentosRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de aplicación para consultar descuentos procesados
 */
@Service
@Transactional(readOnly = true)
public class ConsultaDescuentosProcesadosService {

    private static final Logger logger = LoggerFactory.getLogger(ConsultaDescuentosProcesadosService.class);

    private final ConsultaDescuentosRepository consultaRepository;

    public ConsultaDescuentosProcesadosService(ConsultaDescuentosRepository consultaRepository) {
        this.consultaRepository = consultaRepository;
    }

    /**
     * Obtiene descuentos procesados por periodo y seccional con paginación
     */
    public Page<DescuentoProcesado> obtenerDescuentosProcesados(String periodo, Integer seccionalId, Pageable pageable) {
        logger.info("Consultando descuentos procesados - Período: {}, Seccional: {}, Página: {}",
                   periodo, seccionalId, pageable.getPageNumber());

        validarParametros(periodo, seccionalId);

        Page<DescuentoProcesado> descuentos = consultaRepository.buscarPorPeriodoYSeccional(periodo, seccionalId, pageable);

        logger.info("Encontrados {} descuentos procesados de {} totales para período {} y seccional {}",
                   descuentos.getNumberOfElements(), descuentos.getTotalElements(), periodo, seccionalId);

        return descuentos;
    }

    /**
     * Obtiene todos los descuentos procesados por periodo y seccional sin paginación
     */
    public List<DescuentoProcesado> obtenerTodosDescuentosProcesados(String periodo, Integer seccionalId) {
        logger.info("Consultando todos los descuentos procesados - Período: {}, Seccional: {}", periodo, seccionalId);

        validarParametros(periodo, seccionalId);

        List<DescuentoProcesado> descuentos = consultaRepository.buscarPorPeriodoYSeccional(periodo, seccionalId);

        logger.info("Encontrados {} descuentos procesados para período {} y seccional {}",
                   descuentos.size(), periodo, seccionalId);

        return descuentos;
    }

    /**
     * Cuenta el total de descuentos procesados por periodo y seccional
     */
    public long contarDescuentosProcesados(String periodo, Integer seccionalId) {
        logger.debug("Contando descuentos procesados - Período: {}, Seccional: {}", periodo, seccionalId);

        validarParametros(periodo, seccionalId);

        long total = consultaRepository.contarPorPeriodoYSeccional(periodo, seccionalId);

        logger.debug("Total de descuentos procesados: {} para período {} y seccional {}",
                    total, periodo, seccionalId);

        return total;
    }

    private void validarParametros(String periodo, Integer seccionalId) {
        if (periodo == null || periodo.trim().isEmpty()) {
            throw new IllegalArgumentException("El período es obligatorio");
        }

        if (seccionalId == null || seccionalId <= 0) {
            throw new IllegalArgumentException("El ID de la seccional debe ser un número positivo");
        }

        // Validar formato del período (YYYY/MM)
        if (!periodo.matches("\\d{4}/\\d{2}")) {
            throw new IllegalArgumentException("El período debe tener el formato YYYY/MM");
        }
    }
}

