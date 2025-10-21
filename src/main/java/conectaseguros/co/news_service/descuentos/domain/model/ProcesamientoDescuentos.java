package conectaseguros.co.news_service.descuentos.domain.model;

import conectaseguros.co.news_service.archivo.domain.model.enums.EstadoArchivo;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;

@Getter
public class ProcesamientoDescuentos {

  private final Long archivoId;
  private final String periodo;
  private final Integer seccionalId;
  private final LocalDateTime fechaInicio;
  private final List<ErrorProcesamiento> erroresEncontrados;
  private EstadoArchivo estado;
  private LocalDateTime fechaFin;
  private int totalLineas;
  private int lineasProcesadas;
  private int lineasExitosas;
  private int lineasConError;
  private int excedentes;
  private int faltantes;
  private int noLlegaron;
  private String mensajeEstado;

  public ProcesamientoDescuentos(Long archivoId, String periodo, Integer seccionalId) {
    this.archivoId = Objects.requireNonNull(archivoId, "ArchivoId no puede ser null");
    this.periodo = Objects.requireNonNull(periodo, "Periodo no puede ser null");
    this.seccionalId = Objects.requireNonNull(seccionalId, "SeccionalId no puede ser null");
    this.fechaInicio = LocalDateTime.now();
    this.estado = EstadoArchivo.PROCESANDO;
    this.mensajeEstado = "Iniciando procesamiento...";
    this.erroresEncontrados = new ArrayList<>();
  }

  public void iniciarProcesamiento(int totalLineas) {
    this.totalLineas = totalLineas;
    this.lineasProcesadas = 0;
    this.lineasExitosas = 0;
    this.lineasConError = 0;
    this.estado = EstadoArchivo.PROCESANDO;
    this.mensajeEstado = String.format("Procesando %d líneas...", totalLineas);
  }

  public void registrarLineaProcesada(boolean exitosa) {
    this.lineasProcesadas++;
    if (exitosa) {
      this.lineasExitosas++;
    } else {
      this.lineasConError++;
    }
  }

  public void registrarError(ErrorProcesamiento error) {
    this.erroresEncontrados.add(error);
    this.lineasConError++;
  }

  public void completarProcesamiento() {
    this.fechaFin = LocalDateTime.now();
    this.estado = EstadoArchivo.PROCESADO;
    this.mensajeEstado =
        String.format(
            "Procesamiento completado. Exitosas: %d, Errores: %d", lineasExitosas, lineasConError);
  }

  public void marcarConError(String mensajeError) {
    this.fechaFin = LocalDateTime.now();
    this.estado = EstadoArchivo.ERROR;
    this.mensajeEstado = mensajeError;
  }

  public double getPorcentajeProgreso() {
    if (totalLineas == 0) return 0.0;
    return (double) lineasProcesadas / totalLineas * 100.0;
  }

  public boolean estaCompleto() {
    return estado == EstadoArchivo.PROCESADO || estado == EstadoArchivo.ERROR;
  }

  public EstadoProcesamiento toEstadoProcesamiento() {
    return new EstadoProcesamiento(
        estado,
        totalLineas,
        lineasProcesadas,
        lineasExitosas,
        lineasConError,
        excedentes,
        faltantes,
        noLlegaron,
        fechaInicio,
        fechaFin,
        mensajeEstado);
  }

  public LocalDateTime getFechaInicio() {
    return fechaInicio;
  }

  public LocalDateTime getFechaFin() {
    return fechaFin;
  }

  public int getExcedentes() {
    return excedentes;
  }

  // Setters para estadísticas
  public void setExcedentes(int excedentes) {
    this.excedentes = excedentes;
  }

  public int getFaltantes() {
    return faltantes;
  }

  public void setFaltantes(int faltantes) {
    this.faltantes = faltantes;
  }

  // ✅ NUEVA: Agregar clientes faltantes encontrados en validación
  public void agregarClientesFaltantes(int clientesFaltantes) {
    this.faltantes += clientesFaltantes;
  }

  public int getNoLlegaron() {
    return noLlegaron;
  }

  public void setNoLlegaron(int noLlegaron) {
    this.noLlegaron = noLlegaron;
  }

  public String getMensajeEstado() {
    return mensajeEstado;
  }

  public List<ErrorProcesamiento> getErroresEncontrados() {
    return List.copyOf(erroresEncontrados);
  }

  @Override
  public String toString() {
    return String.format(
        "ProcesamientoDescuentos{archivo=%d, estado=%s, progreso=%.1f%%}",
        archivoId, estado, getPorcentajeProgreso());
  }
}
