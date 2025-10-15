package conectaseguros.co.news_service.archivo.domain.port.input;

import conectaseguros.co.news_service.archivo.domain.model.enums.TipoArchivo;
import org.springframework.web.multipart.MultipartFile;

public interface CargarArchivoUseCase {
    Long cargarArchivo(MultipartFile archivo, TipoArchivo tipoArchivo,
                       String periodo, Integer seccionalId, Integer usuarioId);
}