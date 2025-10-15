package conectaseguros.co.news_service.archivo.domain.model.enums;

import lombok.Getter;

@Getter
public enum EstadoArchivo {
    CARGADO("CARGADO", "Archivo cargado, pendiente de procesamiento"),
    PROCESANDO("PROCESANDO", "Procesamiento en curso"),
    PROCESADO("PROCESADO", "Procesamiento completado exitosamente"),
    ERROR("ERROR", "Error durante el procesamiento");

    private final String valor;
    private final String descripcion;

    EstadoArchivo(String valor, String descripcion) {
        this.valor = valor;
        this.descripcion = descripcion;
    }

    public static EstadoArchivo fromString(String valor) {
        for (EstadoArchivo estado : EstadoArchivo.values()) {
            if (estado.valor.equalsIgnoreCase(valor) || estado.name().equalsIgnoreCase(valor)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de archivo no válido: " + valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}