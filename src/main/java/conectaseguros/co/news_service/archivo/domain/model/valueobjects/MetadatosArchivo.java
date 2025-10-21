package conectaseguros.co.news_service.archivo.domain.model.valueobjects;

import java.time.LocalDateTime;

public record MetadatosArchivo(
    String nombreArchivo,
    String tipoMime,
    Long tamanoBytes,
    String checksumSha256,
    LocalDateTime fechaCarga,
    Integer usuarioCarga) {}
