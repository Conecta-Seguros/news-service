package conectaseguros.co.news_service.archivo.domain.port.output;

import conectaseguros.co.news_service.archivo.domain.model.ArchivoNovedades;
import java.util.Optional;

public interface ArchivoNovedadesRepository {
    ArchivoNovedades guardar(ArchivoNovedades archivo);
    Optional<ArchivoNovedades> buscarPorId(Long id);
    Optional<ArchivoNovedades> buscarPorNombreYPeriodo(String nombre, String periodo);
}