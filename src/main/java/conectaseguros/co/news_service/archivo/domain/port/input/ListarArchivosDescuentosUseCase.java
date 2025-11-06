package conectaseguros.co.news_service.archivo.domain.port.input;

import java.util.Map;

public interface ListarArchivosDescuentosUseCase {

  /**
   * Lista todos los archivos de descuentos
   *
   * @return Mapa con la cantidad y lista de archivos de descuentos
   */
  Map<String, Object> listarArchivosDescuentos();
}
