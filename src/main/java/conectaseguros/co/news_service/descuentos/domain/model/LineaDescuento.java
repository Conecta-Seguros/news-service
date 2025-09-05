package conectaseguros.co.news_service.descuentos.domain.model;

import conectaseguros.co.news_service.descuentos.domain.exceptions.LineaDescuentoInvalidaException;

import java.math.BigDecimal;
import java.util.Map;
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
    private Integer clienteId;

    public LineaDescuento(Map<String, String> camposPorNombre, int numeroLinea) {
        this.numeroLinea = numeroLinea;
        // Solo exigir CODEMPLEADO y VALOR como obligatorios
        this.codigoEmpleado = validarCedulaEmpleado(camposPorNombre.get("CODEMPLEADO"), numeroLinea);
        this.valor = validarValor(camposPorNombre.get("VALOR"), numeroLinea);
        // Los demás campos son opcionales
        this.tercero = camposPorNombre.get("TERCERO") != null ? camposPorNombre.get("TERCERO").trim() : "";
        this.nit = camposPorNombre.get("NIT") != null ? camposPorNombre.get("NIT").trim() : "";
        this.codigoConcepto = camposPorNombre.get("CODCONCEPTO") != null ? camposPorNombre.get("CODCONCEPTO").trim() : "";
        this.concepto = camposPorNombre.get("CONCEPTO") != null ? camposPorNombre.get("CONCEPTO").trim() : "";
        this.nombre1 = camposPorNombre.get("Nombre1") != null ? camposPorNombre.get("Nombre1").trim() : "";
        this.nombre2 = camposPorNombre.get("Nombre2") != null ? camposPorNombre.get("Nombre2").trim() : "";
        this.apellido1 = camposPorNombre.get("Apellido1") != null ? camposPorNombre.get("Apellido1").trim() : "";
        this.apellido2 = camposPorNombre.get("Apellido2") != null ? camposPorNombre.get("Apellido2").trim() : "";
        this.codigoUnidadEjecutora = camposPorNombre.get("COD_UNIDAD_EJECUTORA") != null ? camposPorNombre.get("COD_UNIDAD_EJECUTORA").trim() : "";
        this.nomina = camposPorNombre.get("NOMINA") != null ? camposPorNombre.get("NOMINA").trim() : "";
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
            // Eliminar puntos de miles y convertir comas decimales a punto
            String valorLimpio = valor.replace(".", "").replace(",", ".");
            BigDecimal valorDecimal = new BigDecimal(valorLimpio);

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
    public Integer getClienteId() {
        return clienteId;
    }
    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

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
