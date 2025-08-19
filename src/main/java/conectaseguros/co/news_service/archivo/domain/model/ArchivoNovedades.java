package conectaseguros.co.news_service.archivo.domain.model;

import conectaseguros.co.news_service.archivo.domain.model.enums.EstadoArchivo;
import conectaseguros.co.news_service.archivo.domain.model.valueobjects.MetadatosArchivo;

public class ArchivoNovedades {
    private final String nombreArchivo;
    private final String periodo;
    private final Integer seccionalId;
    private final String rutaArchivo;
    private final byte[] contenido;
    private final MetadatosArchivo metadatos;
    private Long id;
    private Integer totalRegistros;
    private Integer registrosExitosos;
    private Integer registrosErrores;
    private Integer excedentes;
    private Integer faltantes;
    private Integer noLlegaron;
    private EstadoArchivo estado;

    public ArchivoNovedades(String nombreArchivo, String periodo, Integer seccionalId,
                            String rutaArchivo, byte[] contenido, MetadatosArchivo metadatos) {
        this.nombreArchivo = nombreArchivo;
        this.periodo = periodo;
        this.seccionalId = seccionalId;
        this.totalRegistros = 0;
        this.registrosExitosos = 0;
        this.registrosErrores = 0;
        this.excedentes = 0;
        this.faltantes = 0;
        this.noLlegaron = 0;
        this.estado = EstadoArchivo.PROCESANDO;
        this.rutaArchivo = rutaArchivo;
        this.contenido = contenido;
        this.metadatos = metadatos;
    }

    // Métodos de negocio
    public void marcarComoProcesado() {
        this.estado = EstadoArchivo.PROCESADO;
    }

    public void marcarComoError() {
        this.estado = EstadoArchivo.ERROR;
    }

    public void actualizarContadores(Integer exitosos, Integer errores,
                                     Integer excedentes, Integer faltantes, Integer noLlegaron) {
        this.registrosExitosos = exitosos;
        this.registrosErrores = errores;
        this.excedentes = excedentes;
        this.faltantes = faltantes;
        this.noLlegaron = noLlegaron;
        this.totalRegistros = exitosos + errores;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombreArchivo() { return nombreArchivo; }
    public String getPeriodo() { return periodo; }
    public Integer getSeccionalId() { return seccionalId; }
    public Integer getTotalRegistros() { return totalRegistros; }
    public Integer getRegistrosExitosos() { return registrosExitosos; }
    public Integer getRegistrosErrores() { return registrosErrores; }
    public Integer getExcedentes() { return excedentes; }
    public Integer getFaltantes() { return faltantes; }
    public Integer getNoLlegaron() { return noLlegaron; }
    public EstadoArchivo getEstado() { return estado; }
    public String getRutaArchivo() { return rutaArchivo; }
    public byte[] getContenido() { return contenido; }
    public MetadatosArchivo getMetadatos() { return metadatos; }
}