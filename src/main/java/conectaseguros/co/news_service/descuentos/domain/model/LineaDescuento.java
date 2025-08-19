package conectaseguros.co.news_service.descuentos.domain.model;

import conectaseguros.co.news_service.descuentos.domain.exceptions.LineaDescuentoInvalidaException;

import java.math.BigDecimal;
import java.util.Objects;

public class LineaDescuento {

    private final String tercero;
    private final String nit;
    private final String codigoConcepto;
    private final String concepto;
    private final String codigoEmpleado;
    private final String nombre1;
    private final String nombre2;
    private final String apellido1;
    private final String apellido2;
    private final String codigoUnidadEjecutora;
    private final BigDecimal valor;
    private final String nomina;
    private final int numeroLinea;

    public LineaDescuento(String[] campos, int numeroLinea) {
        if (campos == null || campos.length < 12) {
            throw new LineaDescuentoInvalidaException(
                    String.format("Línea %d: Se esperan 12 campos, se encontraron %d",
                            numeroLinea, campos != null ? campos.length : 0)
            );
        }

        this.numeroLinea = numeroLinea;
        this.tercero = validarCampoObligatorio(campos[0], "TERCERO", numeroLinea);
        this.nit = validarCampoObligatorio(campos[1], "NIT", numeroLinea);
        this.codigoConcepto = validarCampoObligatorio(campos[2], "CODCONCEPTO", numeroLinea);
        this.concepto = validarCampoObligatorio(campos[3], "CONCEPTO", numeroLinea);
        this.codigoEmpleado = validarCedulaEmpleado(campos[4], numeroLinea);
        this.nombre1 = validarCampoObligatorio(campos[5], "Nombre1", numeroLinea);
        this.nombre2 = campos[6] != null ? campos[6].trim() : ""; // Opcional
        this.apellido1 = validarCampoObligatorio(campos[7], "Apellido1", numeroLinea);
        this.apellido2 = campos[8] != null ? campos[8].trim() : ""; // Opcional
        this.codigoUnidadEjecutora = validarCampoObligatorio(campos[9], "COD_UNIDAD_EJECUTORA", numeroLinea);
        this.valor = validarValor(campos[10], numeroLinea);
        this.nomina = validarCampoObligatorio(campos[11], "NOMINA", numeroLinea);
    }

    private String validarCampoObligatorio(String campo, String nombreCampo, int numeroLinea) {
        if (campo == null || campo.trim().isEmpty()) {
            throw new LineaDescuentoInvalidaException(
                    String.format("Línea %d: El campo '%s' es obligatorio", numeroLinea, nombreCampo)
            );
        }
        return campo.trim();
    }

    private String validarCedulaEmpleado(String cedula, int numeroLinea) {
        String cedulaLimpia = validarCampoObligatorio(cedula, "CODEMPLEADO", numeroLinea);

        if (!cedulaLimpia.matches("\\d+")) {
            throw new LineaDescuentoInvalidaException(
                    String.format("Línea %d: CODEMPLEADO debe ser numérico, se encontró: '%s'",
                            numeroLinea, cedulaLimpia)
            );
        }

        if (cedulaLimpia.length() < 6 || cedulaLimpia.length() > 15) {
            throw new LineaDescuentoInvalidaException(
                    String.format("Línea %d: CODEMPLEADO debe tener entre 6 y 15 dígitos, se encontró: '%s'",
                            numeroLinea, cedulaLimpia)
            );
        }

        return cedulaLimpia;
    }

    private BigDecimal validarValor(String valor, int numeroLinea) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new LineaDescuentoInvalidaException(
                    String.format("Línea %d: El campo VALOR es obligatorio", numeroLinea)
            );
        }

        try {
            BigDecimal valorDecimal = new BigDecimal(valor.trim());

            if (valorDecimal.compareTo(BigDecimal.ZERO) <= 0) {
                throw new LineaDescuentoInvalidaException(
                        String.format("Línea %d: VALOR debe ser positivo, se encontró: %s",
                                numeroLinea, valor)
                );
            }

            return valorDecimal;

        } catch (NumberFormatException e) {
            throw new LineaDescuentoInvalidaException(
                    String.format("Línea %d: VALOR debe ser numérico, se encontró: '%s'",
                            numeroLinea, valor)
            );
        }
    }

    public String getNombreCompleto() {
        StringBuilder nombre = new StringBuilder(nombre1);

        if (!nombre2.isEmpty()) {
            nombre.append(" ").append(nombre2);
        }

        nombre.append(" ").append(apellido1);

        if (!apellido2.isEmpty()) {
            nombre.append(" ").append(apellido2);
        }

        return nombre.toString();
    }

    // Getters
    public String getTercero() { return tercero; }
    public String getNit() { return nit; }
    public String getCodigoConcepto() { return codigoConcepto; }
    public String getConcepto() { return concepto; }
    public String getCodigoEmpleado() { return codigoEmpleado; }
    public String getNombre1() { return nombre1; }
    public String getNombre2() { return nombre2; }
    public String getApellido1() { return apellido1; }
    public String getApellido2() { return apellido2; }
    public String getCodigoUnidadEjecutora() { return codigoUnidadEjecutora; }
    public BigDecimal getValor() { return valor; }
    public String getNomina() { return nomina; }
    public int getNumeroLinea() { return numeroLinea; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineaDescuento that = (LineaDescuento) o;
        return Objects.equals(codigoEmpleado, that.codigoEmpleado) &&
                Objects.equals(codigoConcepto, that.codigoConcepto) &&
                Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigoEmpleado, codigoConcepto, valor);
    }

    @Override
    public String toString() {
        return String.format("LineaDescuento{línea=%d, empleado='%s', concepto='%s', valor=%s}",
                numeroLinea, codigoEmpleado, codigoConcepto, valor);
    }
}
