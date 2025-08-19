package conectaseguros.co.news_service.descuentos.infrastructure.output.mapper;

import conectaseguros.co.news_service.archivo.domain.model.enums.TipoArchivo;
import conectaseguros.co.news_service.descuentos.domain.model.ErrorProcesamiento;
import conectaseguros.co.news_service.descuentos.domain.model.enums.TipoError;
import conectaseguros.co.news_service.descuentos.infrastructure.output.entity.ErrorProcesamientoEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ErrorProcesamientoMapper {

    /**
     * Convierte de entidad a modelo de dominio
     */
    public ErrorProcesamiento toDomain(ErrorProcesamientoEntity entity) {
        if (entity == null) return null;

        ErrorProcesamiento error = new ErrorProcesamiento(
                entity.getArchivoId(),
                TipoArchivo.valueOf(entity.getTipoArchivo()),
                entity.getLineaArchivo(),
                entity.getCedulaCliente(),
                entity.getPeriodo(),
                entity.getSeccionalId(),
                TipoError.valueOf(entity.getTipoError()),
                entity.getDescripcionError(),
                entity.getContenidoLinea()
        );

        // Asignar ID y fecha si existen
        if (entity.getId() != null) {
            error.setId(entity.getId());
        }

        return error;
    }

    /**
     * Convierte de modelo de dominio a entidad
     */
    public ErrorProcesamientoEntity toEntity(ErrorProcesamiento domain) {
        if (domain == null) return null;

        ErrorProcesamientoEntity entity = new ErrorProcesamientoEntity(
                domain.getArchivoId(),
                domain.getTipoArchivo().name(),
                domain.getLineaArchivo(),
                domain.getCedulaCliente(),
                domain.getPeriodo(),
                domain.getSeccionalId(),
                domain.getTipoError().name(),
                domain.getDescripcionError(),
                domain.getContenidoLinea()
        );

        // Asignar ID si existe
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }

        // Asignar fecha de error
        entity.setFechaError(domain.getFechaError());

        return entity;
    }

    /**
     * Convierte lista de entidades a lista de dominio
     */
    public List<ErrorProcesamiento> toDomainList(List<ErrorProcesamientoEntity> entities) {
        if (entities == null) return List.of();

        return entities.stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * Convierte lista de dominio a lista de entidades
     */
    public List<ErrorProcesamientoEntity> toEntityList(List<ErrorProcesamiento> domains) {
        if (domains == null) return List.of();

        return domains.stream()
                .map(this::toEntity)
                .toList();
    }
}