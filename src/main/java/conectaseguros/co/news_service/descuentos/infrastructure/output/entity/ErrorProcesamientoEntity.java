package conectaseguros.co.news_service.descuentos.infrastructure.output.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "errores_procesamiento")
public class ErrorProcesamientoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "archivo_id", nullable = false)
    private Long archivoId;

    @Column(name = "tipo_archivo", nullable = false, length = 20)
    private String tipoArchivo;

    @Column(name = "linea_archivo")
    private Integer lineaArchivo;

    @Column(name = "cedula_cliente", length = 20)
    private String cedulaCliente;

    @Column(name = "periodo", length = 7)
    private String periodo;

    @Column(name = "seccional_id")
    private Integer seccionalId;

    @Column(name = "tipo_error", nullable = false, length = 50)
    private String tipoError;

    @Column(name = "descripcion_error", nullable = false, columnDefinition = "TEXT")
    private String descripcionError;

    @Column(name = "contenido_linea", columnDefinition = "TEXT")
    private String contenidoLinea;

    @Column(name = "fecha_error", nullable = false)
    private LocalDateTime fechaError = LocalDateTime.now();

    // Constructor vacío
    public ErrorProcesamientoEntity() {}

    // Constructor completo
    public ErrorProcesamientoEntity(Long archivoId, String tipoArchivo, Integer lineaArchivo,
                                    String cedulaCliente, String periodo, Integer seccionalId,
                                    String tipoError, String descripcionError, String contenidoLinea) {
        this.archivoId = archivoId;
        this.tipoArchivo = tipoArchivo;
        this.lineaArchivo = lineaArchivo;
        this.cedulaCliente = cedulaCliente;
        this.periodo = periodo;
        this.seccionalId = seccionalId;
        this.tipoError = tipoError;
        this.descripcionError = descripcionError;
        this.contenidoLinea = contenidoLinea;
        this.fechaError = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getArchivoId() { return archivoId; }
    public void setArchivoId(Long archivoId) { this.archivoId = archivoId; }

    public String getTipoArchivo() { return tipoArchivo; }
    public void setTipoArchivo(String tipoArchivo) { this.tipoArchivo = tipoArchivo; }

    public Integer getLineaArchivo() { return lineaArchivo; }
    public void setLineaArchivo(Integer lineaArchivo) { this.lineaArchivo = lineaArchivo; }

    public String getCedulaCliente() { return cedulaCliente; }
    public void setCedulaCliente(String cedulaCliente) { this.cedulaCliente = cedulaCliente; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public Integer getSeccionalId() { return seccionalId; }
    public void setSeccionalId(Integer seccionalId) { this.seccionalId = seccionalId; }

    public String getTipoError() { return tipoError; }
    public void setTipoError(String tipoError) { this.tipoError = tipoError; }

    public String getDescripcionError() { return descripcionError; }
    public void setDescripcionError(String descripcionError) { this.descripcionError = descripcionError; }

    public String getContenidoLinea() { return contenidoLinea; }
    public void setContenidoLinea(String contenidoLinea) { this.contenidoLinea = contenidoLinea; }

    public LocalDateTime getFechaError() { return fechaError; }
    public void setFechaError(LocalDateTime fechaError) { this.fechaError = fechaError; }

    @Override
    public String toString() {
        return String.format("ErrorProcesamientoEntity{id=%d, archivo=%d, tipo=%s, línea=%d}",
                id, archivoId, tipoError, lineaArchivo);
    }
}
