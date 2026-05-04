package conectaseguros.co.news_service.archivo.domain.port.input;

import java.util.Map;
import org.springframework.data.domain.Pageable;

public interface ListarArchivosDescuentosUseCase {

  Map<String, Object> listarArchivosDescuentos(Pageable pageable);
}
