package conectaseguros.co.news_service.archivo.application.mapper;

import conectaseguros.co.news_service.archivo.domain.model.ArchivoDescuentos;
import conectaseguros.co.news_service.archivo.domain.model.ArchivoNovedades;
import conectaseguros.co.news_service.archivo.domain.model.enums.TipoArchivo;
import conectaseguros.co.news_service.archivo.infrastructure.input.dto.ArchivoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ArchivoMapper {

    public ArchivoResponseDTO toResponseDTO(ArchivoNovedades archivo) {
        return ArchivoResponseDTO.builder()
                .id(archivo.getId())
                .nombreArchivo(archivo.getNombreArchivo())
                .periodo(archivo.getPeriodo())
                .seccionalId(archivo.getSeccionalId())
                .totalRegistros(archivo.getTotalRegistros())
                .registrosExitosos(archivo.getRegistrosExitosos())
                .registrosErrores(archivo.getRegistrosErrores())
                .excedentes(archivo.getExcedentes())
                .faltantes(archivo.getFaltantes())
                .noLlegaron(archivo.getNoLlegaron())
                .estado(archivo.getEstado().getValor())
                .tipoArchivo(TipoArchivo.NOVEDADES.getValor())
                .fechaCarga(archivo.getMetadatos().fechaCarga())
                .usuarioCarga(archivo.getMetadatos().usuarioCarga())
                .tamanoBytes(archivo.getMetadatos().tamanoBytes())
                .build();
    }

    public ArchivoResponseDTO toResponseDTO(ArchivoDescuentos archivo) {
        return ArchivoResponseDTO.builder()
                .id(archivo.getId())
                .nombreArchivo(archivo.getNombreArchivo())
                .periodo(archivo.getPeriodo())
                .seccionalId(archivo.getSeccionalId())
                .totalRegistros(archivo.getTotalRegistros())
                .registrosExitosos(archivo.getRegistrosProcesados())
                .registrosErrores(0) // Los descuentos no manejan errores de la misma forma
                .excedentes(archivo.getExcedentes())
                .faltantes(archivo.getFaltantes())
                .noLlegaron(archivo.getNoLlegaron())
                .estado(archivo.getEstado().getValor())
                .tipoArchivo(TipoArchivo.DESCUENTOS.getValor())
                .fechaCarga(archivo.getMetadatos().fechaCarga())
                .usuarioCarga(archivo.getMetadatos().usuarioCarga())
                .tamanoBytes(archivo.getMetadatos().tamanoBytes())
                .build();
    }
}