package conectaseguros.co.news_service.service;

import conectaseguros.co.news_service.dto.DescuentoDTO;
import conectaseguros.co.news_service.model.Descuento;
import conectaseguros.co.news_service.repository.DescuentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DescuentoService {
    private final DescuentoRepository descuentoRepository;

    public DescuentoDTO crear(DescuentoDTO dto) {
        Descuento descuento = Descuento.builder()
                .clienteId(dto.getClienteId())
                .seccionalId(dto.getSeccionalId())
                .fecha(dto.getFecha())
                .valor(dto.getValor())
                .valorEsperado(dto.getValorEsperado())
                .diferencia(dto.getDiferencia())
                .estado(dto.getEstado() != null ? dto.getEstado() : "PENDIENTE")
                .archivoId(dto.getArchivoId())
                .build();
        return toDTO(descuentoRepository.save(descuento));
    }

    public DescuentoDTO actualizar(Long id, DescuentoDTO dto) {
        Descuento descuento = descuentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Descuento no encontrado"));
        descuento.setClienteId(dto.getClienteId());
        descuento.setSeccionalId(dto.getSeccionalId());
        descuento.setFecha(dto.getFecha());
        descuento.setValor(dto.getValor());
        descuento.setValorEsperado(dto.getValorEsperado());
        descuento.setDiferencia(dto.getDiferencia());
        descuento.setEstado(dto.getEstado());
        descuento.setArchivoId(dto.getArchivoId());
        return toDTO(descuentoRepository.save(descuento));
    }

    public void eliminar(Long id) {
        descuentoRepository.deleteById(id);
    }

    public DescuentoDTO obtener(Long id) {
        return toDTO(descuentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Descuento no encontrado")));
    }

    public List<DescuentoDTO> listar() {
        return descuentoRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    private DescuentoDTO toDTO(Descuento d) {
        return DescuentoDTO.builder()
                .id(d.getId())
                .clienteId(d.getClienteId())
                .seccionalId(d.getSeccionalId())
                .fecha(d.getFecha())
                .valor(d.getValor())
                .valorEsperado(d.getValorEsperado())
                .diferencia(d.getDiferencia())
                .estado(d.getEstado())
                .archivoId(d.getArchivoId())
                .fechaCreacion(d.getFechaCreacion())
                .build();
    }
}
