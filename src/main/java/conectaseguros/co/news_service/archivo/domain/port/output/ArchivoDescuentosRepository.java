package conectaseguros.co.news_service.archivo.domain.port.output;

import conectaseguros.co.news_service.archivo.domain.model.ArchivoDescuentos;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArchivoDescuentosRepository {
  ArchivoDescuentos guardar(ArchivoDescuentos archivo);

  Optional<ArchivoDescuentos> buscarPorId(Long id);

  Optional<ArchivoDescuentos> buscarPorNombreYPeriodo(String nombre, String periodo);

  List<ArchivoDescuentos> obtenerTodos();

  Page<ArchivoDescuentos> obtenerTodosPaginado(Pageable pageable);
}
