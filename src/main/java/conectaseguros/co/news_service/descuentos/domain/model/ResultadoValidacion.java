package conectaseguros.co.news_service.descuentos.domain.model;

import conectaseguros.co.news_service.descuentos.domain.model.enums.TipoError;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ResultadoValidacion {

    private final boolean valido;
    private final List<ErrorValidacion> errores;
    private final Object datosAdicionales;

    private ResultadoValidacion(boolean valido, List<ErrorValidacion> errores, Object datosAdicionales) {
        this.valido = valido;
        this.errores = errores != null ? List.copyOf(errores) : Collections.emptyList();
        this.datosAdicionales = datosAdicionales;
    }

    public static ResultadoValidacion exitoso() {
        return new ResultadoValidacion(true, Collections.emptyList(), null);
    }

    public static ResultadoValidacion exitoso(Object datosAdicionales) {
        return new ResultadoValidacion(true, Collections.emptyList(), datosAdicionales);
    }

    public static ResultadoValidacion conErrores(List<ErrorValidacion> errores) {
        return new ResultadoValidacion(false, errores, null);
    }

    public static ResultadoValidacion conError(ErrorValidacion error) {
        return new ResultadoValidacion(false, List.of(error), null);
    }

    public static ResultadoValidacion conError(TipoError tipoError, String descripcion) {
        return conError(new ErrorValidacion(tipoError, descripcion));
    }

    public boolean esValido() {
        return valido;
    }

    public boolean tieneErrores() {
        return !valido;
    }

    public List<ErrorValidacion> getErrores() {
        return errores;
    }

    public String getDescripcionErrores() {
        return errores.stream()
                .map(ErrorValidacion::getDescripcion)
                .reduce((e1, e2) -> e1 + "; " + e2)
                .orElse("");
    }

    public <T> T getDatosAdicionales(Class<T> tipo) {
        if (datosAdicionales != null && tipo.isInstance(datosAdicionales)) {
            return tipo.cast(datosAdicionales);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResultadoValidacion that = (ResultadoValidacion) o;
        return valido == that.valido && Objects.equals(errores, that.errores);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valido, errores);
    }

    @Override
    public String toString() {
        return String.format("ResultadoValidacion{válido=%s, errores=%d}", valido, errores.size());
    }
}
