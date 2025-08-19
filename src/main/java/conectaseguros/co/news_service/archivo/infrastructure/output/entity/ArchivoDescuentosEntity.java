package conectaseguros.co.news_service.archivo.infrastructure.output.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "archivos_descuentos")
public class ArchivoDescuentosEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "periodo", nullable = false, length = 7)
    private String periodo;

    @Column(name = "seccional_id", nullable = false)
    private Integer seccionalId;

    @Column(name = "total_registros", nullable = false)
    private Integer totalRegistros = 0;

    @Column(name = "registros_procesados", nullable = false)
    private Integer registrosProcesados = 0;

    @Column(name = "diferencias_encontradas", nullable = false)
    private Integer diferenciasEncontradas = 0;

    @Column(name = "excedentes", nullable = false)
    private Integer excedentes = 0;

    @Column(name = "faltantes", nullable = false)
    private Integer faltantes = 0;

    @Column(name = "no_llegaron", nullable = false)
    private Integer noLlegaron = 0;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "PROCESANDO";

    @Column(name = "ruta_archivo", length = 500)
    private String rutaArchivo;

    @Column(name = "contenido")
    @JdbcTypeCode(SqlTypes.BINARY)
    private byte[] contenido;

    @Column(name = "checksum_sha256", length = 64)
    private String checksumSha256;

    @Column(name = "tamano_bytes")
    private Long tamanoBytes;

    @Column(name = "tipo_mime", length = 100)
    private String tipoMime;

    @Column(name = "fecha_carga", nullable = false)
    private LocalDateTime fechaCarga = LocalDateTime.now();

    @Column(name = "usuario_carga", nullable = false)
    private Integer usuarioCarga;

    // Constructores
    public ArchivoDescuentosEntity() {}

    public ArchivoDescuentosEntity(String nombreArchivo, String periodo, Integer seccionalId,
                                   String rutaArchivo, byte[] contenido, String checksumSha256,
                                   Long tamanoBytes, String tipoMime, Integer usuarioCarga) {
        this.nombreArchivo = nombreArchivo;
        this.periodo = periodo;
        this.seccionalId = seccionalId;
        this.rutaArchivo = rutaArchivo;
        this.contenido = contenido;
        this.checksumSha256 = checksumSha256;
        this.tamanoBytes = tamanoBytes;
        this.tipoMime = tipoMime;
        this.usuarioCarga = usuarioCarga;
        this.fechaCarga = LocalDateTime.now();
    }

    // Getters y Setters (similares a ArchivoNovedadesEntity)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public Integer getSeccionalId() { return seccionalId; }
    public void setSeccionalId(Integer seccionalId) { this.seccionalId = seccionalId; }

    public Integer getTotalRegistros() { return totalRegistros; }
    public void setTotalRegistros(Integer totalRegistros) { this.totalRegistros = totalRegistros; }

    public Integer getRegistrosProcesados() { return registrosProcesados; }
    public void setRegistrosProcesados(Integer registrosProcesados) { this.registrosProcesados = registrosProcesados; }

    public Integer getDiferenciasEncontradas() { return diferenciasEncontradas; }
    public void setDiferenciasEncontradas(Integer diferenciasEncontradas) { this.diferenciasEncontradas = diferenciasEncontradas; }

    public Integer getExcedentes() { return excedentes; }
    public void setExcedentes(Integer excedentes) { this.excedentes = excedentes; }

    public Integer getFaltantes() { return faltantes; }
    public void setFaltantes(Integer faltantes) { this.faltantes = faltantes; }

    public Integer getNoLlegaron() { return noLlegaron; }
    public void setNoLlegaron(Integer noLlegaron) { this.noLlegaron = noLlegaron; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }

    public byte[] getContenido() { return contenido; }
    public void setContenido(byte[] contenido) { this.contenido = contenido; }

    public String getChecksumSha256() { return checksumSha256; }
    public void setChecksumSha256(String checksumSha256) { this.checksumSha256 = checksumSha256; }

    public Long getTamanoBytes() { return tamanoBytes; }
    public void setTamanoBytes(Long tamanoBytes) { this.tamanoBytes = tamanoBytes; }

    public String getTipoMime() { return tipoMime; }
    public void setTipoMime(String tipoMime) { this.tipoMime = tipoMime; }

    public LocalDateTime getFechaCarga() { return fechaCarga; }
    public void setFechaCarga(LocalDateTime fechaCarga) { this.fechaCarga = fechaCarga; }

    public Integer getUsuarioCarga() { return usuarioCarga; }
    public void setUsuarioCarga(Integer usuarioCarga) { this.usuarioCarga = usuarioCarga; }
}