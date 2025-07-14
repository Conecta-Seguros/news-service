package conectaseguros.co.news_service.service;

import conectaseguros.co.news_service.dto.ArchivoNovedadesDTO;
import conectaseguros.co.news_service.model.ArchivoNovedades;
import conectaseguros.co.news_service.repository.ArchivoNovedadesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArchivoNovedadesService {
    private final ArchivoNovedadesRepository archivoNovedadesRepository;

    public ArchivoNovedadesDTO crear(ArchivoNovedadesDTO dto) {
        ArchivoNovedades archivo = ArchivoNovedades.builder()
                .nombreArchivo(dto.getNombreArchivo())
                .periodo(dto.getPeriodo())
                .seccionalId(dto.getSeccionalId())
                .totalRegistros(dto.getTotalRegistros())
                .registrosExitosos(dto.getRegistrosExitosos())
                .registrosErrores(dto.getRegistrosErrores())
                .estado(dto.getEstado() != null ? dto.getEstado() : "PROCESANDO")
                .rutaArchivo(dto.getRutaArchivo())
                .usuarioCarga(dto.getUsuarioCarga())
                .build();
        return toDTO(archivoNovedadesRepository.save(archivo));
    }

    public ArchivoNovedadesDTO actualizar(Long id, ArchivoNovedadesDTO dto) {
        ArchivoNovedades archivo = archivoNovedadesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Archivo de novedades no encontrado"));
        archivo.setNombreArchivo(dto.getNombreArchivo());
        archivo.setPeriodo(dto.getPeriodo());
        archivo.setSeccionalId(dto.getSeccionalId());
        archivo.setTotalRegistros(dto.getTotalRegistros());
        archivo.setRegistrosExitosos(dto.getRegistrosExitosos());
        archivo.setRegistrosErrores(dto.getRegistrosErrores());
        archivo.setEstado(dto.getEstado());
        archivo.setRutaArchivo(dto.getRutaArchivo());
        archivo.setUsuarioCarga(dto.getUsuarioCarga());
        return toDTO(archivoNovedadesRepository.save(archivo));
    }

    public void eliminar(Long id) {
        archivoNovedadesRepository.deleteById(id);
    }

    public ArchivoNovedadesDTO obtener(Long id) {
        return toDTO(archivoNovedadesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Archivo de novedades no encontrado")));
    }

    public List<ArchivoNovedadesDTO> listar() {
        return archivoNovedadesRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    private ArchivoNovedadesDTO toDTO(ArchivoNovedades a) {
        return ArchivoNovedadesDTO.builder()
                .id(a.getId())
                .nombreArchivo(a.getNombreArchivo())
                .periodo(a.getPeriodo())
                .seccionalId(a.getSeccionalId())
                .totalRegistros(a.getTotalRegistros())
                .registrosExitosos(a.getRegistrosExitosos())
                .registrosErrores(a.getRegistrosErrores())
                .estado(a.getEstado())
                .rutaArchivo(a.getRutaArchivo())
                .fechaCarga(a.getFechaCarga())
                .usuarioCarga(a.getUsuarioCarga())
                .build();
    }
}
