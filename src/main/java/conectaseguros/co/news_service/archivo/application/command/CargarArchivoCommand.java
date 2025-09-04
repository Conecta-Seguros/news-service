package conectaseguros.co.news_service.archivo.application.command;

import conectaseguros.co.news_service.archivo.domain.model.enums.TipoArchivo;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class CargarArchivoCommand {

    private final MultipartFile archivo;
    private final TipoArchivo tipoArchivo;
    private final String periodo;
    private final Integer seccionalId;
    private final Integer usuarioId;

    public CargarArchivoCommand(MultipartFile archivo, TipoArchivo tipoArchivo,
                                String periodo, Integer seccionalId, Integer usuarioId) {
        this.archivo = archivo;
        this.tipoArchivo = tipoArchivo;
        this.periodo = periodo;
        this.seccionalId = seccionalId;
        this.usuarioId = usuarioId;
    }

}