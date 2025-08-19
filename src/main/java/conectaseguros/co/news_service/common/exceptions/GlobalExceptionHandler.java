package conectaseguros.co.news_service.common.exceptions;

import conectaseguros.co.news_service.archivo.domain.exceptions.ArchivoException;
import conectaseguros.co.news_service.archivo.domain.exceptions.ArchivoNoEncontradoException;
import conectaseguros.co.news_service.archivo.domain.exceptions.ArchivoNoValidoException;
import conectaseguros.co.news_service.common.util.ApiResponse;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ArchivoNoValidoException.class)
    public ResponseEntity<ApiResponse<Object>> handleArchivoNoValido(ArchivoNoValidoException ex) {
        logger.warn("Archivo no válido: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.getMessage(), "Error de validación de archivo"));
    }

    @ExceptionHandler(ArchivoNoEncontradoException.class)
    public ResponseEntity<ApiResponse<Object>> handleArchivoNoEncontrado(ArchivoNoEncontradoException ex) {
        logger.warn("Archivo no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "Archivo no encontrado"));
    }

    @ExceptionHandler(ArchivoException.class)
    public ResponseEntity<ApiResponse<Object>> handleArchivoException(ArchivoException ex) {
        logger.error("Error de archivo: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.getMessage(), "Error interno del sistema"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        logger.warn("Archivo excede el tamaño máximo permitido");
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("El archivo excede el tamaño máximo permitido", "Error de tamaño"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errores.put(fieldName, errorMessage);
        });

        logger.warn("Errores de validación: {}", errores);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(errores.toString(), "Errores de validación"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        logger.error("Error inesperado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error interno del servidor", "Error inesperado"));
    }
}