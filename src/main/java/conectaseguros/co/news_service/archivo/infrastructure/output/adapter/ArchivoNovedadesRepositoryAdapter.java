package conectaseguros.co.news_service.archivo.infrastructure.output.adapter;

import conectaseguros.co.news_service.archivo.domain.model.ArchivoNovedades;
import conectaseguros.co.news_service.archivo.domain.port.output.ArchivoNovedadesRepository;
import conectaseguros.co.news_service.archivo.infrastructure.output.entity.ArchivoNovedadesEntity;
import conectaseguros.co.news_service.archivo.infrastructure.output.mapper.ArchivoJpaMapper;
import conectaseguros.co.news_service.archivo.infrastructure.output.repository.JpaArchivoNovedadesRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ArchivoNovedadesRepositoryAdapter implements ArchivoNovedadesRepository {

    private static final Logger logger = LoggerFactory.getLogger(ArchivoNovedadesRepositoryAdapter.class);

    private final JpaArchivoNovedadesRepository jpaRepository;
    private final ArchivoJpaMapper mapper;

    public ArchivoNovedadesRepositoryAdapter(JpaArchivoNovedadesRepository jpaRepository,
                                             ArchivoJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public ArchivoNovedades guardar(ArchivoNovedades archivo) {
        logger.debug("Guardando archivo de novedades: {}", archivo.getNombreArchivo());

        ArchivoNovedadesEntity entity = mapper.toEntity(archivo);
        ArchivoNovedadesEntity savedEntity = jpaRepository.save(entity);

        logger.debug("Archivo de novedades guardado con ID: {}", savedEntity.getId());
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<ArchivoNovedades> buscarPorId(Long id) {
        logger.debug("Buscando archivo de novedades por ID: {}", id);

        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<ArchivoNovedades> buscarPorNombreYPeriodo(String nombre, String periodo) {
        logger.debug("Buscando archivo de novedades por nombre: {} y periodo: {}", nombre, periodo);

        return jpaRepository.findByNombreArchivoAndPeriodo(nombre, periodo)
                .map(mapper::toDomain);
    }
}