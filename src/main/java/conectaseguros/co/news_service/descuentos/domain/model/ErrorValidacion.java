package conectaseguros.co.news_service.descuentos.domain.model;

import conectaseguros.co.news_service.descuentos.domain.model.enums.TipoError;

import java.util.Objects;

public class ErrorValidacion {

    private final TipoError tipoError;
    private final String descripcion;
    private final String campoAfectado;
    private final Object valorRechazado;

    public ErrorValidacion(TipoError tipoError, String descripcion) {
        this(tipoError, descripcion, null, null);
    }

    public ErrorValidacion(TipoError tipoError, String descripcion, String campoAfectado) {
        this(tipoError, descripcion, campoAfectado, null);
    }

    public ErrorValidacion(TipoError tipoError, String descripcion, String campoAfectado, Object valorRechazado) {
        this.tipoError = Objects.requireNonNull(tipoError, "TipoError no puede ser null");
        this.descripcion = Objects.requireNonNull(descripcion, "Descripción no puede ser null");
        this.campoAfectado = campoAfectado;
        this.valorRechazado = valorRechazado;
    }

    public TipoError getTipoError() {
        return tipoError;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCampoAfectado() {
        return campoAfectado;
    }

    public Object getValorRechazado() {
        return valorRechazado;
    }

    public boolean esErrorCritico() {
        return tipoError.esCritico();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorValidacion that = (ErrorValidacion) o;
        return tipoError == that.tipoError &&
                Objects.equals(descripcion, that.descripcion) &&
                Objects.equals(campoAfectado, that.campoAfectado);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipoError, descripcion, campoAfectado);
    }

    @Override
    public String toString() {
        return String.format("ErrorValidacion{tipo=%s, descripción='%s', campo='%s'}",
                tipoError, descripcion, campoAfectado);
    }
}