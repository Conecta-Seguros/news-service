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
                .cedulaCliente(dto.getCedulaCliente())
                .numeroPoliza(dto.getNumeroPoliza())
                .tipoNovedad(dto.getTipoNovedad())
                .valor(dto.getValor())
                .porcentaje(dto.getPorcentaje())
                .descripcion(dto.getDescripcion())
                .fechaAplicacion(dto.getFechaAplicacion())
                .fechaVencimiento(dto.getFechaVencimiento())
                .estado(dto.getEstado() != null ? dto.getEstado() : "ACTIVO")
                .archivoOrigen(dto.getArchivoOrigen())
                .numeroLinea(dto.getNumeroLinea())
                .usuarioCreacion(dto.getUsuarioCreacion())
                .build();

        return toDTO(novedadRepository.save(novedad));
    }

    public NovedadDTO actualizar(Long id, NovedadDTO dto) {
        Novedad novedad = novedadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Novedad no encontrada"));

        novedad.setTipoNovedad(dto.getTipoNovedad());
        novedad.setValor(dto.getValor());
        novedad.setPorcentaje(dto.getPorcentaje());
        novedad.setDescripcion(dto.getDescripcion());
        novedad.setFechaAplicacion(dto.getFechaAplicacion());
        novedad.setFechaVencimiento(dto.getFechaVencimiento());
        novedad.setEstado(dto.getEstado());

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

    public List<NovedadDTO> listarPorCliente(String cedulaCliente) {
        return novedadRepository.findByCedulaCliente(cedulaCliente)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<NovedadDTO> listarPorPoliza(String numeroPoliza) {
        return novedadRepository.findByNumeroPoliza(numeroPoliza)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private NovedadDTO toDTO(Novedad n) {
        return NovedadDTO.builder()
                .id(n.getId())
                .clienteId(n.getClienteId())
                .cedulaCliente(n.getCedulaCliente())
                .numeroPoliza(n.getNumeroPoliza())
                .tipoNovedad(n.getTipoNovedad())
                .valor(n.getValor())
                .porcentaje(n.getPorcentaje())
                .descripcion(n.getDescripcion())
                .fechaAplicacion(n.getFechaAplicacion())
                .fechaVencimiento(n.getFechaVencimiento())
                .estado(n.getEstado())
                .archivoOrigen(n.getArchivoOrigen())
                .numeroLinea(n.getNumeroLinea())
                .fechaCreacion(n.getFechaCreacion())
                .usuarioCreacion(n.getUsuarioCreacion())
                .build();
    }
}
