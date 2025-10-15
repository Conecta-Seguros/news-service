package conectaseguros.co.news_service.archivo.domain.model;

import conectaseguros.co.news_service.archivo.domain.model.enums.EstadoArchivo;
import conectaseguros.co.news_service.archivo.domain.model.valueobjects.MetadatosArchivo;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
public class ArchivoDescuentos {
    private final String nombreArchivo;
    private final String periodo;
    private final Integer seccionalId;
    private final String rutaArchivo;
    private final byte[] contenido;
    private final MetadatosArchivo metadatos;
    private Long id;
    private Integer totalRegistros;
    private Integer registrosProcesados;
    private Integer diferenciasEncontradas;
    private Integer excedentes;
    private Integer faltantes;
    private Integer noLlegaron;
    private LocalDateTime fechaCarga;
    private EstadoArchivo estado;

    public ArchivoDescuentos(String nombreArchivo, String periodo, Integer seccionalId,
                             String rutaArchivo, byte[] contenido, MetadatosArchivo metadatos) {
        this.nombreArchivo = nombreArchivo;
        this.periodo = periodo;
        this.seccionalId = seccionalId;
        this.totalRegistros = 0;
        this.registrosProcesados = 0;
        this.diferenciasEncontradas = 0;
        this.excedentes = 0;
        this.faltantes = 0;
        this.noLlegaron = 0;
        this.estado = EstadoArchivo.CARGADO;
        this.rutaArchivo = rutaArchivo;
        this.contenido = contenido;
        this.fechaCarga = LocalDateTime.now();
        this.metadatos = metadatos;
    }

    // Métodos de negocio
    public void marcarComoProcesado() {
        this.estado = EstadoArchivo.PROCESADO;
    }

    public void marcarComoError() {
        this.estado = EstadoArchivo.ERROR;
    }

    public void actualizarContadores(Integer procesados, Integer diferencias,
                                     Integer excedentes, Integer faltantes, Integer noLlegaron) {
        this.registrosProcesados = procesados;
        this.diferenciasEncontradas = diferencias;
        this.excedentes = excedentes;
        this.faltantes = faltantes;
        this.noLlegaron = noLlegaron;
        this.totalRegistros = procesados;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombreArchivo() { return nombreArchivo; }
    public String getPeriodo() { return periodo; }
    public Integer getSeccionalId() { return seccionalId; }
    public Integer getTotalRegistros() { return totalRegistros; }
    public Integer getRegistrosProcesados() { return registrosProcesados; }
    public Integer getDiferenciasEncontradas() { return diferenciasEncontradas; }
    public Integer getExcedentes() { return excedentes; }
    public Integer getFaltantes() { return faltantes; }
    public Integer getNoLlegaron() { return noLlegaron; }
    public EstadoArchivo getEstado() { return estado; }
    public String getRutaArchivo() { return rutaArchivo; }
    public byte[] getContenido() { return contenido; }
    public MetadatosArchivo getMetadatos() { return metadatos; }
    public LocalDateTime getFechaCarga() { return fechaCarga; }
    public void setFechaCarga(LocalDateTime fechaCarga) { this.fechaCarga = fechaCarga; }
}