package conectaseguros.co.news_service.service;

import conectaseguros.co.news_service.dto.NovedadDTO;
import conectaseguros.co.news_service.model.Novedad;
import conectaseguros.co.news_service.repository.NovedadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NovedadService {
    private final NovedadRepository novedadRepository;

    public NovedadDTO crear(NovedadDTO dto) {
        Novedad novedad = Novedad.builder()
                .clienteId(dto.getClienteId())
                .aseguradoraId(dto.getAseguradoraId())
                .seccionalId(dto.getSeccionalId())
                .descripcion(dto.getDescripcion())
                .cuota(dto.getCuota())
                .valor(dto.getValor())
                .nomina(dto.getNomina())
                .fecha(dto.getFecha())
                .periodo(dto.getPeriodo())
                .estado(dto.getEstado() != null ? dto.getEstado() : "PENDIENTE")
                .archivoId(dto.getArchivoId())
                .build();
        return toDTO(novedadRepository.save(novedad));
    }

    public NovedadDTO actualizar(Long id, NovedadDTO dto) {
        Novedad novedad = novedadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Novedad no encontrada"));
        novedad.setClienteId(dto.getClienteId());
        novedad.setAseguradoraId(dto.getAseguradoraId());
        novedad.setSeccionalId(dto.getSeccionalId());
        novedad.setDescripcion(dto.getDescripcion());
        novedad.setCuota(dto.getCuota());
        novedad.setValor(dto.getValor());
        novedad.setNomina(dto.getNomina());
        novedad.setFecha(dto.getFecha());
        novedad.setPeriodo(dto.getPeriodo());
        novedad.setEstado(dto.getEstado());
        novedad.setArchivoId(dto.getArchivoId());
        return toDTO(novedadRepository.save(novedad));
    }

    public void eliminar(Long id) {
        novedadRepository.deleteById(id);
    }

    public NovedadDTO obtener(Long id) {
        return toDTO(novedadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Novedad no encontrada")));
    }

    public List<NovedadDTO> listar() {
        return novedadRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    private NovedadDTO toDTO(Novedad n) {
        return NovedadDTO.builder()
                .id(n.getId())
                .clienteId(n.getClienteId())
                .aseguradoraId(n.getAseguradoraId())
                .seccionalId(n.getSeccionalId())
                .descripcion(n.getDescripcion())
                .cuota(n.getCuota())
                .valor(n.getValor())
                .nomina(n.getNomina())
                .fecha(n.getFecha())
                .periodo(n.getPeriodo())
                .estado(n.getEstado())
                .archivoId(n.getArchivoId())
                .fechaCreacion(n.getFechaCreacion())
                .build();
    }
}
