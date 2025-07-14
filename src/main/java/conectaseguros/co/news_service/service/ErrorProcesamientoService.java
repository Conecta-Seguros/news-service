package conectaseguros.co.news_service.service;

import conectaseguros.co.news_service.dto.ErrorProcesamientoDTO;
import conectaseguros.co.news_service.model.ErrorProcesamiento;
import conectaseguros.co.news_service.repository.ErrorProcesamientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ErrorProcesamientoService {
    private final ErrorProcesamientoRepository errorProcesamientoRepository;

    public ErrorProcesamientoDTO crear(ErrorProcesamientoDTO dto) {
        ErrorProcesamiento error = ErrorProcesamiento.builder()
                .archivoId(dto.getArchivoId())
                .tipoArchivo(dto.getTipoArchivo())
                .lineaArchivo(dto.getLineaArchivo())
                .cedulaCliente(dto.getCedulaCliente())
                .tipoError(dto.getTipoError())
                .descripcionError(dto.getDescripcionError())
                .build();
        return toDTO(errorProcesamientoRepository.save(error));
    }

    public ErrorProcesamientoDTO actualizar(Long id, ErrorProcesamientoDTO dto) {
        ErrorProcesamiento error = errorProcesamientoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Error de procesamiento no encontrado"));
        error.setArchivoId(dto.getArchivoId());
        error.setTipoArchivo(dto.getTipoArchivo());
        error.setLineaArchivo(dto.getLineaArchivo());
        error.setCedulaCliente(dto.getCedulaCliente());
        error.setTipoError(dto.getTipoError());
        error.setDescripcionError(dto.getDescripcionError());
        return toDTO(errorProcesamientoRepository.save(error));
    }

    public void eliminar(Long id) {
        errorProcesamientoRepository.deleteById(id);
    }

    public ErrorProcesamientoDTO obtener(Long id) {
        return toDTO(errorProcesamientoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Error de procesamiento no encontrado")));
    }

    public List<ErrorProcesamientoDTO> listar() {
        return errorProcesamientoRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    private ErrorProcesamientoDTO toDTO(ErrorProcesamiento e) {
        return ErrorProcesamientoDTO.builder()
                .id(e.getId())
                .archivoId(e.getArchivoId())
                .tipoArchivo(e.getTipoArchivo())
                .lineaArchivo(e.getLineaArchivo())
                .cedulaCliente(e.getCedulaCliente())
                .tipoError(e.getTipoError())
                .descripcionError(e.getDescripcionError())
                .fechaError(e.getFechaError())
                .build();
    }
}
