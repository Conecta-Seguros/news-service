package conectaseguros.co.news_service.service;

import conectaseguros.co.news_service.dto.ArchivoDescuentosDTO;
import conectaseguros.co.news_service.model.ArchivoDescuentos;
import conectaseguros.co.news_service.repository.ArchivoDescuentosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArchivoDescuentosService {
    private final ArchivoDescuentosRepository archivoDescuentosRepository;

    public ArchivoDescuentosDTO crear(ArchivoDescuentosDTO dto) {
        ArchivoDescuentos archivo = ArchivoDescuentos.builder()
                .nombreArchivo(dto.getNombreArchivo())
                .periodo(dto.getPeriodo())
                .seccionalId(dto.getSeccionalId())
                .totalRegistros(dto.getTotalRegistros())
                .registrosProcesados(dto.getRegistrosProcesados())
                .diferenciasEncontradas(dto.getDiferenciasEncontradas())
                .estado(dto.getEstado() != null ? dto.getEstado() : "PROCESANDO")
                .rutaArchivo(dto.getRutaArchivo())
                .usuarioCarga(dto.getUsuarioCarga())
                .build();
        return toDTO(archivoDescuentosRepository.save(archivo));
    }

    public ArchivoDescuentosDTO actualizar(Long id, ArchivoDescuentosDTO dto) {
        ArchivoDescuentos archivo = archivoDescuentosRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Archivo de descuentos no encontrado"));
        archivo.setNombreArchivo(dto.getNombreArchivo());
        archivo.setPeriodo(dto.getPeriodo());
        archivo.setSeccionalId(dto.getSeccionalId());
        archivo.setTotalRegistros(dto.getTotalRegistros());
        archivo.setRegistrosProcesados(dto.getRegistrosProcesados());
        archivo.setDiferenciasEncontradas(dto.getDiferenciasEncontradas());
        archivo.setEstado(dto.getEstado());
        archivo.setRutaArchivo(dto.getRutaArchivo());
        archivo.setUsuarioCarga(dto.getUsuarioCarga());
        return toDTO(archivoDescuentosRepository.save(archivo));
    }

    public void eliminar(Long id) {
        archivoDescuentosRepository.deleteById(id);
    }

    public ArchivoDescuentosDTO obtener(Long id) {
        return toDTO(archivoDescuentosRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Archivo de descuentos no encontrado")));
    }

    public List<ArchivoDescuentosDTO> listar() {
        return archivoDescuentosRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    private ArchivoDescuentosDTO toDTO(ArchivoDescuentos a) {
        return ArchivoDescuentosDTO.builder()
                .id(a.getId())
                .nombreArchivo(a.getNombreArchivo())
                .periodo(a.getPeriodo())
                .seccionalId(a.getSeccionalId())
                .totalRegistros(a.getTotalRegistros())
                .registrosProcesados(a.getRegistrosProcesados())
                .diferenciasEncontradas(a.getDiferenciasEncontradas())
                .estado(a.getEstado())
                .rutaArchivo(a.getRutaArchivo())
                .fechaCarga(a.getFechaCarga())
                .usuarioCarga(a.getUsuarioCarga())
                .build();
    }
}