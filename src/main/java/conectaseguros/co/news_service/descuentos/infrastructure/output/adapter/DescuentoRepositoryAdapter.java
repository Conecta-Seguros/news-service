package conectaseguros.co.news_service.descuentos.infrastructure.output.adapter;

import conectaseguros.co.news_service.descuentos.domain.model.DescuentoValidado;
import conectaseguros.co.news_service.descuentos.domain.port.output.DescuentoRepository;
import conectaseguros.co.news_service.descuentos.infrastructure.output.entity.DescuentoEntity;
import conectaseguros.co.news_service.descuentos.infrastructure.output.mapper.DescuentoMapper;
import conectaseguros.co.news_service.descuentos.infrastructure.output.repository.DescuentoJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class DescuentoRepositoryAdapter implements DescuentoRepository {

    private static final Logger logger = LoggerFactory.getLogger(DescuentoRepositoryAdapter.class);

    private final DescuentoJpaRepository jpaRepository;
    private final DescuentoMapper mapper;

    public DescuentoRepositoryAdapter(DescuentoJpaRepository jpaRepository, DescuentoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public DescuentoValidado guardar(DescuentoValidado descuento) {
        logger.debug("Guardando descuento: {}", descuento);

        try {
            DescuentoEntity entity = mapper.toEntity(descuento);
            DescuentoEntity savedEntity = jpaRepository.save(entity);
            DescuentoValidado resultado = mapper.toDomain(savedEntity);

            logger.debug("Descuento guardado con ID: {}", savedEntity.getId());
            return resultado;

        } catch (Exception e) {
            logger.error("Error al guardar descuento: {}", descuento, e);
            throw new RuntimeException("Error al guardar descuento", e);
        }
    }

    @Override
    public List<DescuentoValidado> guardarLote(List<DescuentoValidado> descuentos) {
        if (descuentos == null || descuentos.isEmpty()) {
            return List.of();
        }

        logger.debug("Guardando lote de {} descuentos", descuentos.size());

        try {
            List<DescuentoEntity> entities = mapper.toEntityList(descuentos);
            List<DescuentoEntity> savedEntities = jpaRepository.saveAll(entities);
            List<DescuentoValidado> resultado = mapper.toDomainList(savedEntities);

            logger.debug("Lote de {} descuentos guardado exitosamente", savedEntities.size());
            return resultado;

        } catch (Exception e) {
            logger.error("Error al guardar lote de {} descuentos", descuentos.size(), e);
            throw new RuntimeException("Error al guardar lote de descuentos", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DescuentoValidado> buscarPorId(Long id) {
        logger.debug("Buscando descuento por ID: {}", id);

        try {
            return jpaRepository.findById(id)
                    .map(mapper::toDomain);

        } catch (Exception e) {
            logger.error("Error al buscar descuento por ID: {}", id, e);
            throw new RuntimeException("Error al buscar descuento", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeDescuentoPeriodo(String cedulaCliente, String periodo, String codigoConcepto) {
        logger.debug("Verificando existencia de descuento: cliente={}, periodo={}, concepto={}",
                cedulaCliente, periodo, codigoConcepto);

        try {
            boolean existe = jpaRepository.existsByCedulaClienteAndPeriodoAndCodigoDescuento(
                    cedulaCliente, periodo, codigoConcepto);

            logger.debug("Descuento {} para cliente {} en periodo {}",
                    existe ? "existe" : "no existe", cedulaCliente, periodo);
            return existe;

        } catch (Exception e) {
            logger.error("Error al verificar existencia de descuento: cliente={}, periodo={}, concepto={}",
                    cedulaCliente, periodo, codigoConcepto, e);
            throw new RuntimeException("Error al verificar existencia de descuento", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DescuentoValidado> buscarPorArchivo(Long archivoId) {
        logger.debug("Buscando descuentos por archivo: {}", archivoId);

        try {
            List<DescuentoEntity> entities = jpaRepository.findByArchivoIdOrderByIdAsc(archivoId);
            List<DescuentoValidado> resultado = mapper.toDomainList(entities);

            logger.debug("Encontrados {} descuentos para archivo {}", resultado.size(), archivoId);
            return resultado;

        } catch (Exception e) {
            logger.error("Error al buscar descuentos por archivo: {}", archivoId, e);
            throw new RuntimeException("Error al buscar descuentos por archivo", e);
        }
    }

    /**
     * Métodos adicionales para consultas específicas
     */

    @Transactional(readOnly = true)
    public List<DescuentoValidado> buscarPorClienteYPeriodo(String cedulaCliente, String periodo) {
        logger.debug("Buscando descuentos por cliente {} y periodo {}", cedulaCliente, periodo);

        try {
            List<DescuentoEntity> entities = jpaRepository.findByCedulaClienteAndPeriodo(cedulaCliente, periodo);
            return mapper.toDomainList(entities);

        } catch (Exception e) {
            logger.error("Error al buscar descuentos por cliente y periodo", e);
            throw new RuntimeException("Error al buscar descuentos", e);
        }
    }

    @Transactional(readOnly = true)
    public long contarPorArchivo(Long archivoId) {
        try {
            return jpaRepository.countByArchivoId(archivoId);
        } catch (Exception e) {
            logger.error("Error al contar descuentos por archivo: {}", archivoId, e);
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public long contarExitososPorArchivo(Long archivoId) {
        try {
            return jpaRepository.countByArchivoIdAndEstadoProcesado(archivoId);
        } catch (Exception e) {
            logger.error("Error al contar descuentos exitosos por archivo: {}", archivoId, e);
            return 0;
        }
    }
}
