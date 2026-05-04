package conectaseguros.co.news_service.archivo.infrastructure.output.adapter;

import conectaseguros.co.news_service.archivo.domain.model.ArchivoDescuentos;
import conectaseguros.co.news_service.archivo.domain.port.output.ArchivoDescuentosRepository;
import conectaseguros.co.news_service.archivo.infrastructure.output.entity.ArchivoDescuentosEntity;
import conectaseguros.co.news_service.archivo.infrastructure.output.mapper.ArchivoJpaMapper;
import conectaseguros.co.news_service.archivo.infrastructure.output.repository.JpaArchivoDescuentosRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class ArchivoDescuentosRepositoryAdapter implements ArchivoDescuentosRepository {

  private static final Logger logger =
      LoggerFactory.getLogger(ArchivoDescuentosRepositoryAdapter.class);

  private final JpaArchivoDescuentosRepository jpaRepository;
  private final ArchivoJpaMapper mapper;

  public ArchivoDescuentosRepositoryAdapter(
      JpaArchivoDescuentosRepository jpaRepository, ArchivoJpaMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public ArchivoDescuentos guardar(ArchivoDescuentos archivo) {
    logger.debug("Guardando archivo de descuentos: {}", archivo.getNombreArchivo());

    ArchivoDescuentosEntity entity = mapper.toEntity(archivo);
    ArchivoDescuentosEntity savedEntity = jpaRepository.save(entity);

    logger.debug("Archivo de descuentos guardado con ID: {}", savedEntity.getId());
    return mapper.toDomain(savedEntity);
  }

  @Override
  public Optional<ArchivoDescuentos> buscarPorId(Long id) {
    logger.debug("Buscando archivo de descuentos por ID: {}", id);

    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Optional<ArchivoDescuentos> buscarPorNombreYPeriodo(String nombre, String periodo) {
    logger.debug("Buscando archivo de descuentos por nombre: {} y periodo: {}", nombre, periodo);

    return jpaRepository.findByNombreArchivoAndPeriodo(nombre, periodo).map(mapper::toDomain);
  }

  @Override
  public List<ArchivoDescuentos> obtenerTodos() {
    logger.debug("Obteniendo todos los archivos de descuentos");

    List<ArchivoDescuentosEntity> entities = jpaRepository.findAll();

    logger.debug("Se encontraron {} archivos de descuentos", entities.size());

    return entities.stream().map(mapper::toDomain).toList();
  }

  @Override
  public Page<ArchivoDescuentos> obtenerTodosPaginado(Pageable pageable) {
    logger.debug(
        "Obteniendo archivos de descuentos paginados: página {}, tamaño {}",
        pageable.getPageNumber(),
        pageable.getPageSize());

    Page<ArchivoDescuentosEntity> page = jpaRepository.findAllSinContenido(pageable);

    logger.debug(
        "Se encontraron {} archivos de descuentos (total: {})",
        page.getNumberOfElements(),
        page.getTotalElements());

    return page.map(mapper::toDomain);
  }
}
