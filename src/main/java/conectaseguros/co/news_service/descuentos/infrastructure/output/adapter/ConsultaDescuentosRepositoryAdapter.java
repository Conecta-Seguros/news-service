package conectaseguros.co.news_service.descuentos.infrastructure.output.adapter;

import conectaseguros.co.news_service.descuentos.domain.model.DescuentoProcesado;
import conectaseguros.co.news_service.descuentos.domain.port.output.ConsultaDescuentosRepository;
import conectaseguros.co.news_service.descuentos.infrastructure.output.entity.DescuentoEntity;
import conectaseguros.co.news_service.descuentos.infrastructure.output.mapper.DescuentoMapper;
import conectaseguros.co.news_service.descuentos.infrastructure.output.repository.DescuentoJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador de repositorio para consultas de descuentos procesados
 */
@Component
public class ConsultaDescuentosRepositoryAdapter implements ConsultaDescuentosRepository {

    private static final Logger logger = LoggerFactory.getLogger(ConsultaDescuentosRepositoryAdapter.class);

    private final DescuentoJpaRepository jpaRepository;
    private final DescuentoMapper mapper;

    public ConsultaDescuentosRepositoryAdapter(DescuentoJpaRepository jpaRepository, DescuentoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Page<DescuentoProcesado> buscarPorPeriodoYSeccional(String periodo, Integer seccionalId, Pageable pageable) {
        logger.debug("Buscando descuentos con paginación - Período: {}, Seccional: {}, Página: {}",
                    periodo, seccionalId, pageable.getPageNumber());

        Page<DescuentoEntity> entitiesPage = jpaRepository.findBySeccionalAndPeriodo(seccionalId, periodo, pageable);

        return entitiesPage.map(mapper::toDescuentoProcesado);
    }

    @Override
    public List<DescuentoProcesado> buscarPorPeriodoYSeccional(String periodo, Integer seccionalId) {
        logger.debug("Buscando todos los descuentos - Período: {}, Seccional: {}", periodo, seccionalId);

        List<DescuentoEntity> entities = jpaRepository.findBySeccionalAndPeriodo(seccionalId, periodo);

        return mapper.toDescuentoProcesadoList(entities);
    }

    @Override
    public long contarPorPeriodoYSeccional(String periodo, Integer seccionalId) {
        logger.debug("Contando descuentos - Período: {}, Seccional: {}", periodo, seccionalId);

        return jpaRepository.countBySeccionalAndPeriodo(seccionalId, periodo);
    }
}
