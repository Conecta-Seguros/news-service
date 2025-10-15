package conectaseguros.co.news_service.descuentos.domain.exceptions;

public class LineaDescuentoInvalidaException extends RuntimeException {

    private final int numeroLinea;

    public LineaDescuentoInvalidaException(String mensaje) {
        super(mensaje);
        this.numeroLinea = extraerNumeroLinea(mensaje);
    }

    public LineaDescuentoInvalidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.numeroLinea = extraerNumeroLinea(mensaje);
    }

    public LineaDescuentoInvalidaException(int numeroLinea, String mensaje) {
        super(String.format("Línea %d: %s", numeroLinea, mensaje));
        this.numeroLinea = numeroLinea;
    }

    public int getNumeroLinea() {
        return numeroLinea;
    }

    private int extraerNumeroLinea(String mensaje) {
        if (mensaje != null && mensaje.startsWith("Línea ")) {
            try {
                String numero = mensaje.substring(6, mensaje.indexOf(":"));
                return Integer.parseInt(numero);
            } catch (Exception e) {
                return -1;
            }
        }
        return -1;
    }
}