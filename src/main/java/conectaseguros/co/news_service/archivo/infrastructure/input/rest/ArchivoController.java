package conectaseguros.co.news_service.archivo.infrastructure.input.rest;

import conectaseguros.co.news_service.archivo.application.mapper.ArchivoMapper;
import conectaseguros.co.news_service.archivo.domain.model.enums.TipoArchivo;
import conectaseguros.co.news_service.archivo.domain.port.input.CargarArchivoUseCase;
import conectaseguros.co.news_service.archivo.domain.port.input.LeerArchivoUseCase;
import conectaseguros.co.news_service.archivo.infrastructure.input.dto.CargarArchivoRequestDTO;
import conectaseguros.co.news_service.common.util.ApiResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/news/archivos")
public class ArchivoController {

    private static final Logger logger = LoggerFactory.getLogger(ArchivoController.class);

    private final CargarArchivoUseCase cargarArchivoUseCase;
    private final LeerArchivoUseCase leerArchivoUseCase;
    private final ArchivoMapper archivoMapper;

    public ArchivoController(CargarArchivoUseCase cargarArchivoUseCase,
                             LeerArchivoUseCase leerArchivoUseCase,
                             ArchivoMapper archivoMapper) {
        this.cargarArchivoUseCase = cargarArchivoUseCase;
        this.leerArchivoUseCase = leerArchivoUseCase;
        this.archivoMapper = archivoMapper;
    }

    @PostMapping(value = "/cargar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> cargarArchivo(
            @RequestParam("archivo") MultipartFile archivo,
            @Valid @ModelAttribute CargarArchivoRequestDTO request,
            Authentication authentication) {

        try {
            logger.info("Usuario {} iniciando carga de archivo: {} de tipo: {}",
                    authentication.getName(), archivo.getOriginalFilename(), request.getTipoArchivo());

            // Obtener ID del usuario autenticado (simulado por ahora)
            Integer usuarioId = obtenerUsuarioId(authentication);

            TipoArchivo tipoArchivo = TipoArchivo.fromString(request.getTipoArchivo());

            Long archivoId = cargarArchivoUseCase.cargarArchivo(
                    archivo,
                    tipoArchivo,
                    request.getPeriodo(),
                    request.getSeccionalId(),
                    usuarioId
            );

            Map<String, Object> resultado = Map.of(
                    "archivoId", archivoId,
                    "nombreArchivo", archivo.getOriginalFilename(),
                    "periodo", request.getPeriodo(),
                    "tipoArchivo", request.getTipoArchivo(),
                    "estado", "CARGADO"
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(resultado, "Archivo cargado exitosamente"));

        } catch (Exception e) {
            logger.error("Error al cargar archivo: {}", archivo.getOriginalFilename(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error al cargar archivo: " + e.getMessage()));
        }
    }

    @GetMapping("/{archivoId}/leer")
    public ResponseEntity<ApiResponse<Map<String, Object>>> leerArchivo(
            @PathVariable Long archivoId,
            @RequestParam String tipoArchivo,
            @RequestParam(defaultValue = "10") int limite,
            @RequestParam(defaultValue = "0") int offset) {

        try {
            logger.info("Iniciando lectura de archivo ID: {} de tipo: {}", archivoId, tipoArchivo);

            TipoArchivo tipo = TipoArchivo.fromString(tipoArchivo);
            List<String[]> filas = leerArchivoUseCase.leerArchivo(archivoId, tipo);

            // Aplicar paginación
            int totalFilas = filas.size();
            int inicio = Math.min(offset, totalFilas);
            int fin = Math.min(offset + limite, totalFilas);

            List<String[]> filasPaginadas = filas.subList(inicio, fin);

            Map<String, Object> resultado = Map.of(
                    "archivoId", archivoId,
                    "totalFilas", totalFilas,
                    "filasMostradas", filasPaginadas.size(),
                    "offset", offset,
                    "limite", limite,
                    "datos", filasPaginadas
            );

            return ResponseEntity.ok(ApiResponse.success(resultado, "Archivo leído exitosamente"));

        } catch (Exception e) {
            logger.error("Error al leer archivo ID: {}", archivoId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error al leer archivo: " + e.getMessage()));
        }
    }

    @GetMapping("/{archivoId}/preview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> previewArchivo(
            @PathVariable Long archivoId,
            @RequestParam String tipoArchivo) {

        try {
            logger.info("Generando preview de archivo ID: {} de tipo: {}", archivoId, tipoArchivo);

            TipoArchivo tipo = TipoArchivo.fromString(tipoArchivo);
            List<String[]> filas = leerArchivoUseCase.leerArchivo(archivoId, tipo);

            // Solo las primeras 5 filas para preview
            List<String[]> preview = filas.stream()
                    .limit(5)
                    .toList();

            Map<String, Object> resultado = Map.of(
                    "archivoId", archivoId,
                    "totalFilas", filas.size(),
                    "preview", preview,
                    "columnas", preview.isEmpty() ? new String[0] : preview.get(0)
            );

            return ResponseEntity.ok(ApiResponse.success(resultado, "Preview generado exitosamente"));

        } catch (Exception e) {
            logger.error("Error al generar preview del archivo ID: {}", archivoId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error al generar preview: " + e.getMessage()));
        }
    }

    @PostMapping("/validar")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validarArchivo(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam String tipoArchivo) {

        try {
            logger.info("Validando archivo: {} de tipo: {}", archivo.getOriginalFilename(), tipoArchivo);

            // Leer las primeras líneas para validar estructura
            List<String[]> filas = leerArchivoUseCase.leerArchivoDesdeBytes(
                    archivo.getBytes(),
                    archivo.getOriginalFilename()
            );

            boolean esValido = validarEstructuraArchivo(filas, TipoArchivo.fromString(tipoArchivo));

            Map<String, Object> resultado = Map.of(
                    "esValido", esValido,
                    "totalFilas", filas.size(),
                    "primerasFilas", filas.stream().limit(3).toList(),
                    "mensaje", esValido ? "Archivo válido" : "Estructura de archivo incorrecta"
            );

            return ResponseEntity.ok(ApiResponse.success(resultado, "Validación completada"));

        } catch (Exception e) {
            logger.error("Error al validar archivo: {}", archivo.getOriginalFilename(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error al validar archivo: " + e.getMessage()));
        }
    }

    private Integer obtenerUsuarioId(Authentication authentication) {
        // Por ahora retornamos un ID fijo, posteriormente se debe integrar con el servicio de usuarios
        // En una implementación real, extraerías el ID del JWT o del contexto de seguridad
        return 1; // Usuario temporal
    }

    private boolean validarEstructuraArchivo(List<String[]> filas, TipoArchivo tipoArchivo) {
        if (filas.isEmpty()) {
            return false;
        }

        String[] primeraFila = filas.get(0);

        switch (tipoArchivo) {
            case NOVEDADES:
                // Validar que tenga las columnas esperadas para novedades
                return primeraFila.length >= 6; // Ajustar según estructura real
            case DESCUENTOS:
                // Validar que tenga las columnas esperadas para descuentos
                return primeraFila.length >= 5; // Ajustar según estructura real
            default:
                return false;
        }
    }
}