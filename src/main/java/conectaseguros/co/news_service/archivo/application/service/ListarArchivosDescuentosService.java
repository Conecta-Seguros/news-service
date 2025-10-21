package conectaseguros.co.news_service.archivo.application.service;

import conectaseguros.co.news_service.archivo.domain.model.ArchivoDescuentos;
import conectaseguros.co.news_service.archivo.domain.port.input.ListarArchivosDescuentosUseCase;
import conectaseguros.co.news_service.archivo.domain.port.output.ArchivoDescuentosRepository;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  public Map<String, Object> listarArchivosDescuentos() {
    logger.info("Iniciando listado de archivos de descuentos");

    List<ArchivoDescuentos> archivos = archivoDescuentosRepository.obtenerTodos();

    List<Map<String, Object>> archivosDto =
        archivos.stream().map(this::convertirArchivoAMap).toList();

    logger.info("Se encontraron {} archivos de descuentos", archivos.size());

    return Map.of("archivos-encontrados", archivos.size(), "archivos", archivosDto);
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
