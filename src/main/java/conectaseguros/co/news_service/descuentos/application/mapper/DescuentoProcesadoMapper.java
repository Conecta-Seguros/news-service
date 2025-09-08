package conectaseguros.co.news_service.descuentos.application.mapper;

import conectaseguros.co.news_service.descuentos.domain.model.DescuentoProcesado;
import conectaseguros.co.news_service.descuentos.infrastructure.input.dto.DescuentoProcesadoDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper para convertir entre DescuentoProcesado y DescuentoProcesadoDTO
 */
@Component
public class DescuentoProcesadoMapper {

    /**
     * Convierte de modelo de dominio a DTO
     */
    public DescuentoProcesadoDTO toDTO(DescuentoProcesado descuento) {
        if (descuento == null) return null;

        return new DescuentoProcesadoDTO(
                descuento.getId(),
                descuento.getClienteId(),
                descuento.getSeccionalId(),
                descuento.getAseguradoraId(),
                descuento.getCedulaCliente(),
                descuento.getPeriodo(),
                descuento.getFecha(),
                descuento.getCodigoDescuento(),
                descuento.getCodigoValidacion(),
                descuento.getValor(),
                descuento.getValorEsperado(),
                descuento.getDiferencia(),
                descuento.getEstado(),
                descuento.getArchivoId(),
                descuento.getFechaCreacion()
        );
    }

    /**
     * Convierte lista de modelos de dominio a lista de DTOs
     */
    public List<DescuentoProcesadoDTO> toDTOList(List<DescuentoProcesado> descuentos) {
        if (descuentos == null) return null;

        return descuentos.stream()
                .map(this::toDTO)
                .toList();
    }
}
