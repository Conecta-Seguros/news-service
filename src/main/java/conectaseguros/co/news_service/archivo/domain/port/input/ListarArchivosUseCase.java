package conectaseguros.co.news_service.archivo.domain.port.input;

import conectaseguros.co.news_service.archivo.domain.model.enums.EstadoArchivo;
import conectaseguros.co.news_service.archivo.domain.model.enums.TipoArchivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Map;

public interface ListarArchivosUseCase {

    /**
     * Lista todos los archivos con paginación y filtros opcionales
     */
    Page<Map<String, Object>> listarArchivos(
            String tipoArchivo,
            Integer seccionalId,
            EstadoArchivo estado,
            Pageable pageable
    );

    /**
     * Obtiene estadísticas de archivos con filtros opcionales
     */
    Map<String, Object> obtenerEstadisticas(
            String tipoArchivo,
            Integer seccionalId,
            EstadoArchivo estado,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    );
}
