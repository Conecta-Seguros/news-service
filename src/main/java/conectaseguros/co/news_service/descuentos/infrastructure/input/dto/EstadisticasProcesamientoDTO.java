package conectaseguros.co.news_service.descuentos.infrastructure.input.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class EstadisticasProcesamientoDTO {

    @JsonProperty("archivo_id")
    private Long archivoId;

    @JsonProperty("resumen")
    private ResumenDTO resumen;

    @JsonProperty("errores_por_tipo")
    private Map<String, Integer> erroresPorTipo;

    @JsonProperty("top_errores_frecuentes")
    private Map<String, Integer> topErroresFrecuentes;

    @JsonProperty("clientes_con_errores")
    private Integer clientesConErrores;

    @JsonProperty("tiempo_procesamiento_segundos")
    private Long tiempoProcesamiento;

    // Constructor vacío
    public EstadisticasProcesamientoDTO() {}

    public static class ResumenDTO {
        @JsonProperty("total_lineas")
        private Integer totalLineas;

        @JsonProperty("exitosas")
        private Integer exitosas;

        @JsonProperty("con_errores")
        private Integer conErrores;

        @JsonProperty("tasa_exito_porcentaje")
        private Double tasaExito;

        // Constructor vacío
        public ResumenDTO() {}

        public ResumenDTO(Integer totalLineas, Integer exitosas, Integer conErrores) {
            this.totalLineas = totalLineas;
            this.exitosas = exitosas;
            this.conErrores = conErrores;
            this.tasaExito = totalLineas > 0 ? (double) exitosas / totalLineas * 100 : 0.0;
        }

        // Getters y Setters
        public Integer getTotalLineas() { return totalLineas; }
        public void setTotalLineas(Integer totalLineas) { this.totalLineas = totalLineas; }

        public Integer getExitosas() { return exitosas; }
        public void setExitosas(Integer exitosas) { this.exitosas = exitosas; }

        public Integer getConErrores() { return conErrores; }
        public void setConErrores(Integer conErrores) { this.conErrores = conErrores; }

        public Double getTasaExito() { return tasaExito; }
        public void setTasaExito(Double tasaExito) { this.tasaExito = tasaExito; }
    }

    // Getters y Setters
    public Long getArchivoId() { return archivoId; }
    public void setArchivoId(Long archivoId) { this.archivoId = archivoId; }

    public ResumenDTO getResumen() { return resumen; }
    public void setResumen(ResumenDTO resumen) { this.resumen = resumen; }

    public Map<String, Integer> getErroresPorTipo() { return erroresPorTipo; }
    public void setErroresPorTipo(Map<String, Integer> erroresPorTipo) { this.erroresPorTipo = erroresPorTipo; }

    public Map<String, Integer> getTopErroresFrecuentes() { return topErroresFrecuentes; }
    public void setTopErroresFrecuentes(Map<String, Integer> topErroresFrecuentes) { this.topErroresFrecuentes = topErroresFrecuentes; }

    public Integer getClientesConErrores() { return clientesConErrores; }
    public void setClientesConErrores(Integer clientesConErrores) { this.clientesConErrores = clientesConErrores; }

    public Long getTiempoProcesamiento() { return tiempoProcesamiento; }
    public void setTiempoProcesamiento(Long tiempoProcesamiento) { this.tiempoProcesamiento = tiempoProcesamiento; }
}