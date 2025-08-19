package conectaseguros.co.news_service.archivo.domain.model.enums;

public enum EstadoArchivo {
    PROCESANDO("PROCESANDO"),
    PROCESADO("PROCESADO"),
    ERROR("ERROR");

    private final String valor;

    EstadoArchivo(String valor) {
        this.valor = valor;
    }

    public static EstadoArchivo fromString(String valor) {
        for (EstadoArchivo estado : EstadoArchivo.values()) {
            if (estado.valor.equalsIgnoreCase(valor)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de archivo no válido: " + valor);
    }

    public String getValor() {
        return valor;
    }
}