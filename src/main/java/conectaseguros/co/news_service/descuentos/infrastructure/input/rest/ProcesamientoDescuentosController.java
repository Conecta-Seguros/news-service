package conectaseguros.co.news_service.descuentos.infrastructure.input.rest;

import conectaseguros.co.news_service.archivo.domain.model.enums.EstadoArchivo;
import conectaseguros.co.news_service.common.util.ApiResponse;
import conectaseguros.co.news_service.descuentos.application.service.ConsultaProcesamientoService;
import conectaseguros.co.news_service.descuentos.application.service.ProcesarDescuentosService;
import conectaseguros.co.news_service.descuentos.domain.model.ErrorProcesamiento;
import conectaseguros.co.news_service.descuentos.domain.model.EstadoProcesamiento;
import conectaseguros.co.news_service.descuentos.infrastructure.input.dto.ErrorProcesamientoDTO;
import conectaseguros.co.news_service.descuentos.infrastructure.input.dto.EstadisticasProcesamientoDTO;
import conectaseguros.co.news_service.descuentos.infrastructure.input.dto.ProcesamientoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/news/descuentos")
@Tag(name = "Procesamiento de Descuentos", description = "APIs para procesar archivos de descuentos")
public class ProcesamientoDescuentosController {

    private static final Logger logger = LoggerFactory.getLogger(ProcesamientoDescuentosController.class);

    private final ProcesarDescuentosService procesarService;
    private final ConsultaProcesamientoService consultaService;

    public ProcesamientoDescuentosController(ProcesarDescuentosService procesarService,
                                             ConsultaProcesamientoService consultaService) {
        this.procesarService = procesarService;
        this.consultaService = consultaService;
    }

    @PostMapping("/archivos/{archivoId}/procesar")
    @Operation(summary = "Iniciar procesamiento de archivo de descuentos",
            description = "Inicia el procesamiento asíncrono de un archivo de descuentos previamente cargado")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<ApiResponse<ProcesamientoResponseDTO>> procesarArchivo(
            @Parameter(description = "ID del archivo a procesar")
            @PathVariable Long archivoId) {

        try {
            logger.info("Iniciando procesamiento de archivo de descuentos: {}", archivoId);

            // ✅ AGREGAR: Verificar que el archivo existe primero
            EstadoProcesamiento estadoActual = consultaService.obtenerEstadoProcesamiento(archivoId);
            logger.info("Estado actual del archivo {}: {}", archivoId, estadoActual.getEstado());

            // Verificar si ya está en procesamiento
            if (consultaService.estaEnProcesamiento(archivoId)) {
                logger.warn("Archivo {} ya está siendo procesado", archivoId);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("El archivo ya está siendo procesado"));
            }

            // ✅ AGREGAR: Verificar que el estado permite procesamiento
            if (estadoActual.getEstado() == EstadoArchivo.PROCESADO) {
                logger.warn("Archivo {} ya fue procesado", archivoId);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("El archivo ya fue procesado"));
            }

            // Iniciar procesamiento asíncrono
            logger.info("Iniciando procesamiento asíncrono para archivo: {}", archivoId);
            CompletableFuture<EstadoProcesamiento> futuro = procesarService.procesarArchivo(archivoId);

            // ✅ AGREGAR: Log para confirmar que se inició
            logger.info("Procesamiento asíncrono iniciado para archivo: {}", archivoId);

            // Obtener estado inicial actualizado
            EstadoProcesamiento estadoInicial = consultaService.obtenerEstadoProcesamiento(archivoId);
            ProcesamientoResponseDTO response = new ProcesamientoResponseDTO(archivoId, estadoInicial);

            logger.info("Procesamiento iniciado exitosamente para archivo: {}", archivoId);

            return ResponseEntity.accepted()
                    .body(ApiResponse.success(response, "Procesamiento iniciado exitosamente"));

        } catch (Exception e) {
            logger.error("Error al iniciar procesamiento del archivo: {}", archivoId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error al iniciar procesamiento: " + e.getMessage()));
        }
    }

    @GetMapping("/archivos/{archivoId}/estado")
    @Operation(summary = "Consultar estado de procesamiento",
            description = "Obtiene el estado actual del procesamiento de un archivo de descuentos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR') or hasRole('CONSULTA')")
    public ResponseEntity<ApiResponse<ProcesamientoResponseDTO>> consultarEstado(
            @Parameter(description = "ID del archivo")
            @PathVariable Long archivoId) {

        try {
            logger.debug("Consultando estado de procesamiento para archivo: {}", archivoId);

            EstadoProcesamiento estado = consultaService.obtenerEstadoProcesamiento(archivoId);
            ProcesamientoResponseDTO response = new ProcesamientoResponseDTO(archivoId, estado);

            return ResponseEntity.ok(ApiResponse.success(response, "Estado consultado exitosamente"));

        } catch (Exception e) {
            logger.error("Error al consultar estado del archivo: {}", archivoId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error al consultar estado: " + e.getMessage()));
        }
    }

    @GetMapping("/archivos/{archivoId}/errores")
    @Operation(summary = "Obtener errores de procesamiento",
            description = "Lista los errores encontrados durante el procesamiento de un archivo")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR') or hasRole('CONSULTA')")
    public ResponseEntity<ApiResponse<List<ErrorProcesamientoDTO>>> obtenerErrores(
            @Parameter(description = "ID del archivo")
            @PathVariable Long archivoId,
            @Parameter(description = "Número máximo de errores a retornar")
            @RequestParam(defaultValue = "100") int limite,
            @Parameter(description = "Número de errores a saltar")
            @RequestParam(defaultValue = "0") int offset) {

        try {
            logger.debug("Consultando errores para archivo: {} (límite: {}, offset: {})",
                    archivoId, limite, offset);

            List<ErrorProcesamiento> errores = consultaService.obtenerErroresProcesamiento(archivoId, limite, offset);

            List<ErrorProcesamientoDTO> response = errores.stream()
                    .map(ErrorProcesamientoDTO::new)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(response,
                    String.format("Se encontraron %d errores", response.size())));

        } catch (Exception e) {
            logger.error("Error al consultar errores del archivo: {}", archivoId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error al consultar errores: " + e.getMessage()));
        }
    }

    @GetMapping("/archivos/{archivoId}/estadisticas")
    @Operation(summary = "Obtener estadísticas de procesamiento",
            description = "Genera estadísticas detalladas del procesamiento de un archivo")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR') or hasRole('CONSULTA')")
    public ResponseEntity<ApiResponse<EstadisticasProcesamientoDTO>> obtenerEstadisticas(
            @Parameter(description = "ID del archivo")
            @PathVariable Long archivoId) {

        try {
            logger.debug("Generando estadísticas para archivo: {}", archivoId);

            EstadisticasProcesamientoDTO estadisticas = consultaService.generarEstadisticas(archivoId);

            return ResponseEntity.ok(ApiResponse.success(estadisticas, "Estadísticas generadas exitosamente"));

        } catch (Exception e) {
            logger.error("Error al generar estadísticas del archivo: {}", archivoId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error al generar estadísticas: " + e.getMessage()));
        }
    }

    @GetMapping("/archivos/{archivoId}/progreso")
    @Operation(summary = "Obtener progreso de procesamiento",
            description = "Obtiene el porcentaje de progreso del procesamiento en tiempo real")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR') or hasRole('CONSULTA')")
    public ResponseEntity<ApiResponse<Double>> obtenerProgreso(
            @Parameter(description = "ID del archivo")
            @PathVariable Long archivoId) {

        try {
            double progreso = consultaService.obtenerPorcentajeProgreso(archivoId);

            return ResponseEntity.ok(ApiResponse.success(progreso,
                    String.format("Progreso: %.1f%%", progreso)));

        } catch (Exception e) {
            logger.error("Error al consultar progreso del archivo: {}", archivoId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error al consultar progreso: " + e.getMessage()));
        }
    }

    @GetMapping("/archivos/{archivoId}/debug-completo")
    @Operation(summary = "Debug completo del archivo")
    public ResponseEntity<Map<String, Object>> debugCompleto(@PathVariable Long archivoId) {

        try {
            // Consultar directamente desde BD
            EstadoProcesamiento estado = consultaService.obtenerEstadoProcesamiento(archivoId);

            Map<String, Object> debug = Map.of(
                    "archivo_id", archivoId,
                    "estado_dominio", estado.getEstado().name(),
                    "estado_valor", estado.getEstado().getValor(),
                    "en_procesamiento", consultaService.estaEnProcesamiento(archivoId),
                    "total_lineas", estado.getTotalLineas(),
                    "procesadas", estado.getLineasProcesadas(),
                    "timestamp", java.time.LocalDateTime.now()
            );

            return ResponseEntity.ok(debug);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
