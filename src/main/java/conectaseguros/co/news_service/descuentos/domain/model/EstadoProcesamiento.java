package conectaseguros.co.news_service.descuentos.domain.model;

import conectaseguros.co.news_service.archivo.domain.model.enums.EstadoArchivo;
import java.time.LocalDateTime;
import java.util.Objects;

public class EstadoProcesamiento {

  private final EstadoArchivo estado;
  private final int totalLineas;
  private final int lineasProcesadas;
  private final int lineasExitosas;
  private final int lineasConError;
  private final int excedentes;
  private final int faltantes;
  private final int noLlegaron;
  private final LocalDateTime fechaInicio;
  private final LocalDateTime fechaFin;
  private final String mensajeEstado;

  public EstadoProcesamiento(
      EstadoArchivo estado,
      int totalLineas,
      int lineasProcesadas,
      int lineasExitosas,
      int lineasConError,
      int excedentes,
      int faltantes,
      int noLlegaron,
      LocalDateTime fechaInicio,
      LocalDateTime fechaFin,
      String mensajeEstado) {
    this.estado = Objects.requireNonNull(estado);
    this.totalLineas = Math.max(0, totalLineas);
    this.lineasProcesadas = Math.max(0, lineasProcesadas);
    this.lineasExitosas = Math.max(0, lineasExitosas);
    this.lineasConError = Math.max(0, lineasConError);
    this.excedentes = Math.max(0, excedentes);
    this.faltantes = Math.max(0, faltantes);
    this.noLlegaron = Math.max(0, noLlegaron);
    this.fechaInicio = fechaInicio;
    this.fechaFin = fechaFin;
    this.mensajeEstado = mensajeEstado;
  }

  public static EstadoProcesamiento iniciando(int totalLineas) {
    return new EstadoProcesamiento(
        EstadoArchivo.PROCESANDO,
        totalLineas,
        0,
        0,
        0,
        0,
        0,
        0,
        LocalDateTime.now(),
        null,
        "Iniciando procesamiento...");
  }

  public static EstadoProcesamiento completado(
      int totalLineas,
      int exitosas,
      int errores,
      int excedentes,
      int faltantes,
      int noLlegaron,
      LocalDateTime fechaInicio) {
    return new EstadoProcesamiento(
        EstadoArchivo.PROCESADO,
        totalLineas,
        totalLineas,
        exitosas,
        errores,
        excedentes,
        faltantes,
        noLlegaron,
        fechaInicio,
        LocalDateTime.now(),
        "Procesamiento completado exitosamente");
  }

  public static EstadoProcesamiento conError(String mensajeError, LocalDateTime fechaInicio) {
    return new EstadoProcesamiento(
        EstadoArchivo.ERROR, 0, 0, 0, 0, 0, 0, 0, fechaInicio, LocalDateTime.now(), mensajeError);
  }

  public double getPorcentajeProgreso() {
    if (totalLineas == 0) return 0.0;
    return (double) lineasProcesadas / totalLineas * 100.0;
  }

  public boolean estaCompleto() {
    return estado == EstadoArchivo.PROCESADO || estado == EstadoArchivo.ERROR;
  }

  public boolean tieneErrores() {
    return lineasConError > 0 || estado == EstadoArchivo.ERROR;
  }

  // Getters
  public EstadoArchivo getEstado() {
    return estado;
  }

  public int getTotalLineas() {
    return totalLineas;
  }

  public int getLineasProcesadas() {
    return lineasProcesadas;
  }

  public int getLineasExitosas() {
    return lineasExitosas;
  }

  public int getLineasConError() {
    return lineasConError;
  }

  public int getExcedentes() {
    return excedentes;
  }

  public int getFaltantes() {
    return faltantes;
  }

  public int getNoLlegaron() {
    return noLlegaron;
  }

  public LocalDateTime getFechaInicio() {
    return fechaInicio;
  }

  public LocalDateTime getFechaFin() {
    return fechaFin;
  }

  public String getMensajeEstado() {
    return mensajeEstado;
  }

  @Override
  public String toString() {
    return String.format(
        "EstadoProcesamiento{estado=%s, progreso=%.1f%%, exitosas=%d, errores=%d}",
        estado, getPorcentajeProgreso(), lineasExitosas, lineasConError);
  }
}
