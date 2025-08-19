package conectaseguros.co.news_service.descuentos.domain.model;

import conectaseguros.co.news_service.archivo.domain.model.enums.TipoArchivo;
import conectaseguros.co.news_service.descuentos.domain.model.enums.TipoError;

import java.time.LocalDateTime;
import java.util.Objects;

public class ErrorProcesamiento {

    private Long id;
    private final Long archivoId;
    private final TipoArchivo tipoArchivo;
    private final Integer lineaArchivo;
    private final String cedulaCliente;
    private final String periodo;
    private final Integer seccionalId;
    private final TipoError tipoError;
    private final String descripcionError;
    private final String contenidoLinea;
    private final LocalDateTime fechaError;

    public ErrorProcesamiento(Long archivoId, TipoArchivo tipoArchivo, Integer lineaArchivo,
                              String cedulaCliente, String periodo, Integer seccionalId,
                              TipoError tipoError, String descripcionError, String contenidoLinea) {
        this.archivoId = Objects.requireNonNull(archivoId, "ArchivoId no puede ser null");
        this.tipoArchivo = Objects.requireNonNull(tipoArchivo, "TipoArchivo no puede ser null");
        this.lineaArchivo = lineaArchivo;
        this.cedulaCliente = cedulaCliente;
        this.periodo = periodo;
        this.seccionalId = seccionalId;
        this.tipoError = Objects.requireNonNull(tipoError, "TipoError no puede ser null");
        this.descripcionError = Objects.requireNonNull(descripcionError, "DescripcionError no puede ser null");
        this.contenidoLinea = contenidoLinea;
        this.fechaError = LocalDateTime.now();
    }

    public static ErrorProcesamiento deLineaDescuento(Long archivoId, LineaDescuento linea,
                                                      ErrorValidacion errorValidacion) {
        return new ErrorProcesamiento(
                archivoId,
                TipoArchivo.DESCUENTOS,
                linea.getNumeroLinea(),
                linea.getCodigoEmpleado(),
                null, // periodo se obtiene del archivo
                null, // seccionalId se obtiene del archivo
                errorValidacion.getTipoError(),
                errorValidacion.getDescripcion(),
                linea.toString()
        );
    }

    public static ErrorProcesamiento deEstructuraArchivo(Long archivoId, TipoError tipoError,
                                                         String descripcion) {
        return new ErrorProcesamiento(
                archivoId,
                TipoArchivo.DESCUENTOS,
                null,
                null,
                null,
                null,
                tipoError,
                descripcion,
                null
        );
    }

    // Getters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getArchivoId() { return archivoId; }
    public TipoArchivo getTipoArchivo() { return tipoArchivo; }
    public Integer getLineaArchivo() { return lineaArchivo; }
    public String getCedulaCliente() { return cedulaCliente; }
    public String getPeriodo() { return periodo; }
    public Integer getSeccionalId() { return seccionalId; }
    public TipoError getTipoError() { return tipoError; }
    public String getDescripcionError() { return descripcionError; }
    public String getContenidoLinea() { return contenidoLinea; }
    public LocalDateTime getFechaError() { return fechaError; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorProcesamiento that = (ErrorProcesamiento) o;
        return Objects.equals(archivoId, that.archivoId) &&
                Objects.equals(lineaArchivo, that.lineaArchivo) &&
                tipoError == that.tipoError;
    }

    @Override
    public int hashCode() {
        return Objects.hash(archivoId, lineaArchivo, tipoError);
    }

    @Override
    public String toString() {
        return String.format("ErrorProcesamiento{archivo=%d, línea=%d, tipo=%s}",
                archivoId, lineaArchivo, tipoError);
    }
}
