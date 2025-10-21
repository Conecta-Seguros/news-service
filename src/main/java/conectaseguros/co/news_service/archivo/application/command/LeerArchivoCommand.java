package conectaseguros.co.news_service.archivo.application.command;

import conectaseguros.co.news_service.archivo.domain.model.enums.TipoArchivo;

public record LeerArchivoCommand(Long archivoId, TipoArchivo tipoArchivo) {}
