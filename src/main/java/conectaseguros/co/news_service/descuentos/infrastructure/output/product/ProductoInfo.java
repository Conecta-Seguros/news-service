package conectaseguros.co.news_service.descuentos.infrastructure.output.product;

import java.math.BigDecimal;

public class ProductoInfo {
    private final Integer id;
    private final String codigo;
    private final String nombre;
    private final Boolean vigente;
    private final BigDecimal valorAnual;
    private final BigDecimal valorCuotaMensual;

    public ProductoInfo(Integer id, String codigo, String nombre, Boolean vigente, BigDecimal valorAnual, BigDecimal valorCuotaMensual) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.vigente = vigente;
        this.valorAnual = valorAnual;
        this.valorCuotaMensual = valorCuotaMensual;
    }

    public Integer getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public Boolean getVigente() { return vigente; }
    public BigDecimal getValorAnual() { return valorAnual; }
    public BigDecimal getValorCuotaMensual() { return valorCuotaMensual; }
}

