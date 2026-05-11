package conectaseguros.co.news_service.archivo.domain.port.output;

import conectaseguros.co.news_service.archivo.domain.model.enums.TipoArchivo;
import java.util.List;

public interface AlmacenadorArchivos {
  String almacenar(byte[] contenido, String nombreArchivo, TipoArchivo tipo, String periodo);

  byte[] recuperar(String rutaArchivo);

  List<String> recuperarComoLineas(String rutaArchivo);

  void eliminar(String rutaArchivo);

  boolean existe(String rutaArchivo);
}
