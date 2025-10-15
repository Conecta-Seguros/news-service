package conectaseguros.co.news_service.archivo.domain.exceptions;

public class ArchivoException extends RuntimeException {
    public ArchivoException(String message) {
        super(message);
    }

    public ArchivoException(String message, Throwable cause) {
        super(message, cause);
    }
}