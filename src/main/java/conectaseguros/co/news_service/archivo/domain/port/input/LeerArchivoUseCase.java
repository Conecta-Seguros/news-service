package conectaseguros.co.news_service.archivo.domain.port.input;

import conectaseguros.co.news_service.archivo.domain.model.enums.TipoArchivo;
import java.util.List;

public interface LeerArchivoUseCase {
  List<String[]> leerArchivo(Long archivoId, TipoArchivo tipoArchivo);

  List<String[]> leerArchivoDesdeBytes(byte[] contenido, String nombreArchivo);
}
