package conectaseguros.co.news_service.archivo.infrastructure.output.mapper;

import conectaseguros.co.news_service.archivo.domain.model.ArchivoDescuentos;
import conectaseguros.co.news_service.archivo.domain.model.ArchivoNovedades;
import conectaseguros.co.news_service.archivo.domain.model.valueobjects.MetadatosArchivo;
import conectaseguros.co.news_service.archivo.infrastructure.output.entity.ArchivoDescuentosEntity;
import conectaseguros.co.news_service.archivo.infrastructure.output.entity.ArchivoNovedadesEntity;
import org.springframework.stereotype.Component;

@Component
public class ArchivoJpaMapper {

    public ArchivoNovedadesEntity toEntity(ArchivoNovedades domain) {
        ArchivoNovedadesEntity entity = new ArchivoNovedadesEntity();
        entity.setId(domain.getId());
        entity.setNombreArchivo(domain.getNombreArchivo());
        entity.setPeriodo(domain.getPeriodo());
        entity.setSeccionalId(domain.getSeccionalId());
        entity.setTotalRegistros(domain.getTotalRegistros());
        entity.setRegistrosExitosos(domain.getRegistrosExitosos());
        entity.setRegistrosErrores(domain.getRegistrosErrores());
        entity.setExcedentes(domain.getExcedentes());
        entity.setFaltantes(domain.getFaltantes());
        entity.setNoLlegaron(domain.getNoLlegaron());
        entity.setEstado(domain.getEstado().getValor());
        entity.setRutaArchivo(domain.getRutaArchivo());
        entity.setContenido(domain.getContenido());

        MetadatosArchivo metadatos = domain.getMetadatos();
        entity.setChecksumSha256(metadatos.checksumSha256());
        entity.setTamanoBytes(metadatos.tamanoBytes());
        entity.setTipoMime(metadatos.tipoMime());
        entity.setFechaCarga(metadatos.fechaCarga());
        entity.setUsuarioCarga(metadatos.usuarioCarga());

        return entity;
    }

    public ArchivoNovedades toDomain(ArchivoNovedadesEntity entity) {
        MetadatosArchivo metadatos = new MetadatosArchivo(
                entity.getNombreArchivo(),
                entity.getTipoMime(),
                entity.getTamanoBytes(),
                entity.getChecksumSha256(),
                entity.getFechaCarga(),
                entity.getUsuarioCarga()
        );

        ArchivoNovedades domain = new ArchivoNovedades(
                entity.getNombreArchivo(),
                entity.getPeriodo(),
                entity.getSeccionalId(),
                entity.getRutaArchivo(),
                entity.getContenido(),
                metadatos
        );

        domain.setId(entity.getId());
        domain.actualizarContadores(
                entity.getRegistrosExitosos(),
                entity.getRegistrosErrores(),
                entity.getExcedentes(),
                entity.getFaltantes(),
                entity.getNoLlegaron()
        );

        if ("PROCESADO".equals(entity.getEstado())) {
            domain.marcarComoProcesado();
        } else if ("ERROR".equals(entity.getEstado())) {
            domain.marcarComoError();
        }

        return domain;
    }

    public ArchivoDescuentosEntity toEntity(ArchivoDescuentos domain) {
        ArchivoDescuentosEntity entity = new ArchivoDescuentosEntity();
        entity.setId(domain.getId());
        entity.setNombreArchivo(domain.getNombreArchivo());
        entity.setPeriodo(domain.getPeriodo());
        entity.setSeccionalId(domain.getSeccionalId());
        entity.setTotalRegistros(domain.getTotalRegistros());
        entity.setRegistrosProcesados(domain.getRegistrosProcesados());
        entity.setDiferenciasEncontradas(domain.getDiferenciasEncontradas());
        entity.setExcedentes(domain.getExcedentes());
        entity.setFaltantes(domain.getFaltantes());
        entity.setNoLlegaron(domain.getNoLlegaron());
        entity.setEstado(domain.getEstado().getValor());
        entity.setRutaArchivo(domain.getRutaArchivo());
        entity.setContenido(domain.getContenido());

        MetadatosArchivo metadatos = domain.getMetadatos();
        entity.setChecksumSha256(metadatos.checksumSha256());
        entity.setTamanoBytes(metadatos.tamanoBytes());
        entity.setTipoMime(metadatos.tipoMime());
        entity.setFechaCarga(metadatos.fechaCarga());
        entity.setUsuarioCarga(metadatos.usuarioCarga());

        return entity;
    }

    public ArchivoDescuentos toDomain(ArchivoDescuentosEntity entity) {
        MetadatosArchivo metadatos = new MetadatosArchivo(
                entity.getNombreArchivo(),
                entity.getTipoMime(),
                entity.getTamanoBytes(),
                entity.getChecksumSha256(),
                entity.getFechaCarga(),
                entity.getUsuarioCarga()
        );

        ArchivoDescuentos domain = new ArchivoDescuentos(
                entity.getNombreArchivo(),
                entity.getPeriodo(),
                entity.getSeccionalId(),
                entity.getRutaArchivo(),
                entity.getContenido(),
                metadatos
        );

        domain.setId(entity.getId());
        domain.actualizarContadores(
                entity.getRegistrosProcesados(),
                entity.getDiferenciasEncontradas(),
                entity.getExcedentes(),
                entity.getFaltantes(),
                entity.getNoLlegaron()
        );

        if ("PROCESADO".equals(entity.getEstado())) {
            domain.marcarComoProcesado();
        } else if ("ERROR".equals(entity.getEstado())) {
            domain.marcarComoError();
        }

        return domain;
    }
}