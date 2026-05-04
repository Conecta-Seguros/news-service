package conectaseguros.co.news_service.archivo.application.service;

import conectaseguros.co.news_service.archivo.domain.model.ArchivoDescuentos;
import conectaseguros.co.news_service.archivo.domain.port.input.ListarArchivosDescuentosUseCase;
import conectaseguros.co.news_service.archivo.domain.port.output.ArchivoDescuentosRepository;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ListarArchivosDescuentosService implements ListarArchivosDescuentosUseCase {

  private static final Logger logger =
      LoggerFactory.getLogger(ListarArchivosDescuentosService.class);

  private final ArchivoDescuentosRepository archivoDescuentosRepository;

  public ListarArchivosDescuentosService(ArchivoDescuentosRepository archivoDescuentosRepository) {
    this.archivoDescuentosRepository = archivoDescuentosRepository;
  }

  @Override
  public Map<String, Object> listarArchivosDescuentos(Pageable pageable) {
    logger.info(
        "Iniciando listado de archivos de descuentos: página {}, tamaño {}",
        pageable.getPageNumber(),
        pageable.getPageSize());

    Page<ArchivoDescuentos> page = archivoDescuentosRepository.obtenerTodosPaginado(pageable);

    List<Map<String, Object>> archivosDto =
        page.getContent().stream().map(this::convertirArchivoAMap).toList();

    logger.info(
        "Se encontraron {} archivos de descuentos (total: {})",
        page.getNumberOfElements(),
        page.getTotalElements());

    return Map.of(
        "archivos",
        archivosDto,
        "paginacion",
        Map.of(
            "pagina", page.getNumber(),
            "tamano", page.getSize(),
            "totalElementos", page.getTotalElements(),
            "totalPaginas", page.getTotalPages(),
            "ultima", page.isLast()));
  }

  private Map<String, Object> convertirArchivoAMap(ArchivoDescuentos archivo) {
    return Map.of(
        "id", archivo.getId(),
        "nombre", archivo.getNombreArchivo(),
        "periodo", archivo.getPeriodo(),
        "seccional", archivo.getSeccionalId(),
        "estado", archivo.getEstado().getValor());
  }
}
