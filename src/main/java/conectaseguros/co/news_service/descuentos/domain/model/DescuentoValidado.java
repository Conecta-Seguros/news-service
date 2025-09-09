package conectaseguros.co.news_service.descuentos.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class DescuentoValidado {

    private final Integer clienteId;
    private final Integer seccionalId;
    private final Integer aseguradoraId;
    private final String cedulaCliente;
    private final String periodo;
    private final LocalDate fecha;
    private final String codigoDescuento;
    private final String codigoValidacion;
    private final BigDecimal valor;
    private final BigDecimal valorEsperado;
    private final BigDecimal diferencia;
    private final Long archivoId;
    private final LocalDateTime fechaCreacion;
    private Long id;
    private String estado;

    public DescuentoValidado(Integer clienteId, Integer seccionalId, Integer aseguradoraId,
                             String cedulaCliente, String periodo, LocalDate fecha,
                             String codigoDescuento, String codigoValidacion, BigDecimal valor,
                             BigDecimal valorEsperado, Long archivoId) {
        this.clienteId = Objects.requireNonNull(clienteId, "ClienteId no puede ser null");
        this.seccionalId = Objects.requireNonNull(seccionalId, "SeccionalId no puede ser null");
        this.aseguradoraId = aseguradoraId;
        this.cedulaCliente = Objects.requireNonNull(cedulaCliente, "CedulaCliente no puede ser null");
        this.periodo = Objects.requireNonNull(periodo, "Periodo no puede ser null");
        this.fecha = Objects.requireNonNull(fecha, "Fecha no puede ser null");
        this.codigoDescuento = Objects.requireNonNull(codigoDescuento, "CodigoDescuento no puede ser null");
        this.codigoValidacion = codigoValidacion;
        this.valor = Objects.requireNonNull(valor, "Valor no puede ser null");
        this.valorEsperado = valorEsperado;
        this.diferencia = calcularDiferencia(valor, valorEsperado);

        // ✅ LÓGICA CORREGIDA: Estado según diferencia
        if (this.diferencia.compareTo(BigDecimal.ZERO) == 0) {
            this.estado = "CONCILIADO";
        } else if (this.diferencia.compareTo(BigDecimal.ZERO) > 0) {
            // Si diferencia > 0 significa valor > valorEsperado → EXCEDENTE
            this.estado = "EXCEDENTE";
        } else {
            // Si diferencia < 0 significa valor < valorEsperado → FALTANTE
            this.estado = "FALTANTE";
        }

        this.archivoId = archivoId;
        this.fechaCreacion = LocalDateTime.now();
    }

    public static DescuentoValidado fromLineaDescuento(LineaDescuento linea, Integer clienteId,
                                                       Integer seccionalId, String periodo,
                                                       Long archivoId, BigDecimal valorEsperado,
                                                       BigDecimal valorTotal) {
        Integer aseguradoraId = extraerAseguradoraId(linea.getNit());
        return new DescuentoValidado(
                clienteId,
                seccionalId,
                aseguradoraId,
                linea.getCodigoEmpleado(),
                periodo,
                LocalDate.now(),
                linea.getCodigoConcepto(),
                null,
                valorTotal, // <-- Usar el total sumado
                valorEsperado,
                archivoId
        );
    }

    private static BigDecimal calcularDiferencia(BigDecimal valor, BigDecimal valorEsperado) {
        if (valorEsperado == null) {
            return BigDecimal.ZERO;
        }
        return valor.subtract(valorEsperado);
    }

    private static Integer extraerAseguradoraId(String nit) {
        // Mapeo simplificado de NIT a ID de aseguradora
        // En producción esto vendría de una tabla o servicio
        switch (nit) {
            case "890903790": return 1; // Suramericana
            case "860002536": return 2; // Sura
            default: return null;
        }
    }

    // Getters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getClienteId() { return clienteId; }
    public Integer getSeccionalId() { return seccionalId; }
    public Integer getAseguradoraId() { return aseguradoraId; }
    public String getCedulaCliente() { return cedulaCliente; }
    public String getPeriodo() { return periodo; }
    public LocalDate getFecha() { return fecha; }
    public String getCodigoDescuento() { return codigoDescuento; }
    public String getCodigoValidacion() { return codigoValidacion; }
    public BigDecimal getValor() { return valor; }
    public BigDecimal getValorEsperado() { return valorEsperado; }
    public BigDecimal getDiferencia() { return diferencia; }
    public String getEstado() { return estado; }

    public void setEstado(String estado) {
        // Setter para permitir modificar el estado desde el servicio
        // (en vez de solo en el constructor)
        // Si se requiere validación, agregar aquí
        // Ejemplo: VALIDADO, FALTANTE, EXCEDENTE
        try {
            java.lang.reflect.Field field = this.getClass().getDeclaredField("estado");
            field.setAccessible(true);
            field.set(this, estado);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo asignar el estado", e);
        }
    }

    public Long getArchivoId() { return archivoId; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DescuentoValidado that = (DescuentoValidado) o;
        return Objects.equals(cedulaCliente, that.cedulaCliente) &&
                Objects.equals(periodo, that.periodo) &&
                Objects.equals(codigoDescuento, that.codigoDescuento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cedulaCliente, periodo, codigoDescuento);
    }

    @Override
    public String toString() {
        return String.format("DescuentoValidado{cliente='%s', período='%s', valor=%s}",
                cedulaCliente, periodo, valor);
    }
}
