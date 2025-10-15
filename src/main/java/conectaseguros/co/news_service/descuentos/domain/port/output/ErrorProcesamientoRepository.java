package conectaseguros.co.news_service.descuentos.domain.port.output;

import conectaseguros.co.news_service.descuentos.domain.model.ErrorProcesamiento;
import conectaseguros.co.news_service.descuentos.domain.model.enums.TipoError;

import java.util.List;

public interface ErrorProcesamientoRepository {

    /**
     * Guarda un error de procesamiento
     * @param error Error a guardar
     * @return Error guardado con ID asignado
     */
    ErrorProcesamiento guardar(ErrorProcesamiento error);

    /**
     * Guarda múltiples errores en lote
     * @param errores Lista de errores a guardar
     * @return Lista de errores guardados
     */
    List<ErrorProcesamiento> guardarLote(List<ErrorProcesamiento> errores);

    /**
     * Busca errores por archivo
     * @param archivoId ID del archivo
     * @return Lista de errores del archivo
     */
    List<ErrorProcesamiento> buscarPorArchivo(Long archivoId);

    /**
     * Busca errores por tipo
     * @param archivoId ID del archivo
     * @param tipoError Tipo de error
     * @return Lista de errores del tipo especificado
     */
    List<ErrorProcesamiento> buscarPorTipoError(Long archivoId, TipoError tipoError);

    /**
     * Cuenta errores por archivo
     * @param archivoId ID del archivo
     * @return Total de errores
     */
    long contarErroresPorArchivo(Long archivoId);

    /**
     * Busca IDs de archivos únicos que tienen errores
     * @return Lista de IDs de archivos con errores
     */
    List<Long> findDistinctArchivoIdsWithErrors();
}
