package conectaseguros.co.news_service.descuentos.infrastructure.output.mapper;

import conectaseguros.co.news_service.descuentos.domain.model.DescuentoValidado;
import conectaseguros.co.news_service.descuentos.infrastructure.output.entity.DescuentoEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DescuentoMapper {

    /**
     * Convierte de entidad a modelo de dominio
     */
    public DescuentoValidado toDomain(DescuentoEntity entity) {
        if (entity == null) return null;

        return new DescuentoValidado(
                entity.getClienteId(),
                entity.getSeccionalId(),
                entity.getAseguradoraId(),
                entity.getCedulaCliente(),
                entity.getPeriodo(),
                entity.getFecha(),
                entity.getCodigoDescuento(),
                entity.getCodigoValidacion(),
                entity.getValor(),
                entity.getValorEsperado(),
                entity.getArchivoId()
        );
    }

    /**
     * Convierte de modelo de dominio a entidad
     */
    public DescuentoEntity toEntity(DescuentoValidado domain) {
        if (domain == null) return null;

        DescuentoEntity entity = new DescuentoEntity(
                domain.getClienteId(),
                domain.getSeccionalId(),
                domain.getAseguradoraId(),
                domain.getCedulaCliente(),
                domain.getPeriodo(),
                domain.getFecha(),
                domain.getCodigoDescuento(),
                domain.getCodigoValidacion(),
                domain.getValor(),
                domain.getValorEsperado(),
                domain.getArchivoId()
        );

        // Asignar ID si existe
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }

        // Asignar campos calculados
        entity.setEstado(domain.getEstado());
        entity.setFechaCreacion(domain.getFechaCreacion());

        // Calcular y asignar la diferencia
        BigDecimal valor = domain.getValor() != null ? domain.getValor() : BigDecimal.ZERO;
        BigDecimal valorEsperado = domain.getValorEsperado() != null ? domain.getValorEsperado() : BigDecimal.ZERO;
        entity.setDiferencia(valor.subtract(valorEsperado));

        return entity;
    }

    /**
     * Actualiza una entidad existente con datos del dominio
     */
    public void updateEntity(DescuentoEntity entity, DescuentoValidado domain) {
        if (entity == null || domain == null) return;

        entity.setClienteId(domain.getClienteId());
        entity.setSeccionalId(domain.getSeccionalId());
        entity.setAseguradoraId(domain.getAseguradoraId());
        entity.setCedulaCliente(domain.getCedulaCliente());
        entity.setPeriodo(domain.getPeriodo());
        entity.setFecha(domain.getFecha());
        entity.setCodigoDescuento(domain.getCodigoDescuento());
        entity.setCodigoValidacion(domain.getCodigoValidacion());
        entity.setValor(domain.getValor());
        entity.setValorEsperado(domain.getValorEsperado());
        entity.setEstado(domain.getEstado());
    }

    /**
     * Convierte lista de entidades a lista de dominio
     */
    public List<DescuentoValidado> toDomainList(List<DescuentoEntity> entities) {
        if (entities == null) return List.of();

        return entities.stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * Convierte lista de dominio a lista de entidades
     */
    public List<DescuentoEntity> toEntityList(List<DescuentoValidado> domains) {
        if (domains == null) return List.of();

        return domains.stream()
                .map(this::toEntity)
                .toList();
    }
}
