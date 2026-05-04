package conectaseguros.co.news_service.descuentos.infrastructure.input.rest;

import conectaseguros.co.news_service.archivo.application.service.LeerArchivoService;
import conectaseguros.co.news_service.archivo.domain.model.enums.EstadoArchivo;
import conectaseguros.co.news_service.archivo.domain.model.enums.TipoArchivo;
import conectaseguros.co.news_service.common.util.ApiResponse;
import conectaseguros.co.news_service.descuentos.application.mapper.DescuentoProcesadoMapper;
import conectaseguros.co.news_service.descuentos.application.service.ConsultaDescuentosProcesadosService;
import conectaseguros.co.news_service.descuentos.application.service.ConsultaProcesamientoService;
import conectaseguros.co.news_service.descuentos.application.service.ProcesarDescuentosService;
import conectaseguros.co.news_service.descuentos.domain.model.DescuentoProcesado;
import conectaseguros.co.news_service.descuentos.domain.model.ErrorProcesamiento;
import conectaseguros.co.news_service.descuentos.domain.model.EstadoProcesamiento;
import conectaseguros.co.news_service.descuentos.infrastructure.input.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/news/descuentos")
@Tag(
    name = "Procesamiento de Descuentos",
    description = "APIs para procesar archivos de descuentos")
public class ProcesamientoDescuentosController {

  private static final Logger logger =
      LoggerFactory.getLogger(ProcesamientoDescuentosController.class);

  private final ProcesarDescuentosService procesarService;
  private final ConsultaProcesamientoService consultaService;
  private final LeerArchivoService leerArchivoService;
  private final ConsultaDescuentosProcesadosService consultaDescuentosService;
  private final DescuentoProcesadoMapper descuentoMapper;

  public ProcesamientoDescuentosController(
      ProcesarDescuentosService procesarService,
      ConsultaProcesamientoService consultaService,
      LeerArchivoService leerArchivoService,
      ConsultaDescuentosProcesadosService consultaDescuentosService,
      DescuentoProcesadoMapper descuentoMapper) {
    this.procesarService = procesarService;
    this.consultaService = consultaService;
    this.leerArchivoService = leerArchivoService;
    this.consultaDescuentosService = consultaDescuentosService;
    this.descuentoMapper = descuentoMapper;
  }

  @PostMapping("/archivos/{archivoId}/procesar")
  @Operation(
      summary = "Iniciar procesamiento de archivo de descuentos",
      description =
          "Inicia el procesamiento asíncrono de un archivo de descuentos previamente cargado")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
  public ResponseEntity<ApiResponse<ProcesamientoResponseDTO>> procesarArchivo(
      @Parameter(description = "ID del archivo a procesar") @PathVariable Long archivoId) {

    try {
      logger.info("Iniciando procesamiento de archivo de descuentos: {}", archivoId);

      EstadoProcesamiento estadoActual = consultaService.obtenerEstadoProcesamiento(archivoId);
      logger.info("Estado actual del archivo {}: {}", archivoId, estadoActual.getEstado());

      if (consultaService.estaEnProcesamiento(archivoId)) {
        logger.warn("Archivo {} ya está siendo procesado", archivoId);
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("El archivo ya está siendo procesado"));
      }

      if (estadoActual.getEstado() == EstadoArchivo.PROCESADO) {
        logger.warn("Archivo {} ya fue procesado", archivoId);
        return ResponseEntity.badRequest().body(ApiResponse.error("El archivo ya fue procesado"));
      }

      // Obtener nombre y tipo de archivo
      String nombreArchivo =
          leerArchivoService.obtenerNombreArchivo(archivoId, TipoArchivo.DESCUENTOS);
      String extension =
          nombreArchivo != null && nombreArchivo.contains(".")
              ? nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1).toLowerCase()
              : "";

      // Leer archivo como filas
      List<String[]> filas =
          leerArchivoService.leerArchivoDesdeBytes(
              leerArchivoService.obtenerContenidoArchivo(archivoId, TipoArchivo.DESCUENTOS),
              nombreArchivo);

      // Procesar archivo nativamente desde filas
      EstadoProcesamiento estadoProcesamiento =
          procesarService.procesarArchivoDesdeFilas(archivoId, filas);

      ProcesamientoResponseDTO response =
          new ProcesamientoResponseDTO(archivoId, estadoProcesamiento);
      logger.info("Procesamiento iniciado exitosamente para archivo: {}", archivoId);

      return ResponseEntity.accepted()
          .body(ApiResponse.success(response, "Procesamiento iniciado exitosamente"));

    } catch (Exception e) {
      logger.error("Error al iniciar procesamiento del archivo: {}", archivoId, e);
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Error al iniciar procesamiento: " + e.getMessage()));
    }
  }

  @DeleteMapping("/archivos/{archivoId}/procesamiento")
  @Operation(
      summary = "Deshacer procesamiento de archivo",
      description =
          "Elimina todos los descuentos y errores asociados a un archivo y restaura su estado a CARGADO")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> deshacerProcesamiento(
      @Parameter(description = "ID del archivo") @PathVariable Long archivoId) {

    try {
      logger.info("Solicitud para deshacer procesamiento del archivo: {}", archivoId);

      procesarService.deshacerProcesamiento(archivoId);

      return ResponseEntity.ok(
          ApiResponse.success(
              null, "Procesamiento deshecho exitosamente. El archivo ha vuelto a estado CARGADO."));

    } catch (IllegalStateException e) {
      logger.warn("Estado inválido para deshacer procesamiento: {}", e.getMessage());
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("No se puede deshacer el procesamiento: " + e.getMessage()));
    } catch (Exception e) {
      logger.error("Error al deshacer procesamiento del archivo: {}", archivoId, e);
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Error al deshacer procesamiento: " + e.getMessage()));
    }
  }

  @GetMapping("/archivos/{archivoId}/estado")
  @Operation(
      summary = "Consultar estado de procesamiento",
      description = "Obtiene el estado actual del procesamiento de un archivo de descuentos")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR') or hasRole('CONSULTA')")
  public ResponseEntity<ApiResponse<ProcesamientoResponseDTO>> consultarEstado(
      @Parameter(description = "ID del archivo") @PathVariable Long archivoId) {

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
  @Operation(
      summary = "Obtener errores de procesamiento",
      description = "Lista los errores encontrados durante el procesamiento de un archivo")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR') or hasRole('CONSULTA')")
  public ResponseEntity<ApiResponse<List<ErrorProcesamientoDTO>>> obtenerErrores(
      @Parameter(description = "ID del archivo") @PathVariable Long archivoId,
      @Parameter(description = "Número máximo de errores a retornar")
          @RequestParam(defaultValue = "100")
          int limite,
      @Parameter(description = "Número de errores a saltar") @RequestParam(defaultValue = "0")
          int offset) {

    try {
      logger.debug(
          "Consultando errores para archivo: {} (límite: {}, offset: {})",
          archivoId,
          limite,
          offset);

      List<ErrorProcesamiento> errores =
          consultaService.obtenerErroresProcesamiento(archivoId, limite, offset);

      List<ErrorProcesamientoDTO> response =
          errores.stream().map(ErrorProcesamientoDTO::new).toList();

      return ResponseEntity.ok(
          ApiResponse.success(
              response, String.format("Se encontraron %d errores", response.size())));

    } catch (Exception e) {
      logger.error("Error al consultar errores del archivo: {}", archivoId, e);
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Error al consultar errores: " + e.getMessage()));
    }
  }

  @GetMapping("/archivos/{archivoId}/estadisticas")
  @Operation(
      summary = "Obtener estadísticas de procesamiento",
      description = "Genera estadísticas detalladas del procesamiento de un archivo")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR') or hasRole('CONSULTA')")
  public ResponseEntity<ApiResponse<EstadisticasProcesamientoDTO>> obtenerEstadisticas(
      @Parameter(description = "ID del archivo") @PathVariable Long archivoId) {

    try {
      logger.debug("Generando estadísticas para archivo: {}", archivoId);

      EstadisticasProcesamientoDTO estadisticas = consultaService.generarEstadisticas(archivoId);

      return ResponseEntity.ok(
          ApiResponse.success(estadisticas, "Estadísticas generadas exitosamente"));

    } catch (Exception e) {
      logger.error("Error al generar estadísticas del archivo: {}", archivoId, e);
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Error al generar estadísticas: " + e.getMessage()));
    }
  }

  @GetMapping("/archivos/{archivoId}/progreso")
  @Operation(
      summary = "Obtener progreso de procesamiento",
      description = "Obtiene el porcentaje de progreso del procesamiento en tiempo real")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR') or hasRole('CONSULTA')")
  public ResponseEntity<ApiResponse<Double>> obtenerProgreso(
      @Parameter(description = "ID del archivo") @PathVariable Long archivoId) {

    try {
      double progreso = consultaService.obtenerPorcentajeProgreso(archivoId);

      return ResponseEntity.ok(
          ApiResponse.success(progreso, String.format("Progreso: %.1f%%", progreso)));

    } catch (Exception e) {
      logger.error("Error al consultar progreso del archivo: {}", archivoId, e);
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Error al consultar progreso: " + e.getMessage()));
    }
  }

  @GetMapping("/procesados")
  @Operation(
      summary = "Consultar descuentos procesados",
      description =
          "Obtiene los descuentos procesados filtrados por período y seccional con paginación opcional")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR') or hasRole('CONSULTA')")
  public ResponseEntity<ApiResponse<Object>> consultarDescuentosProcesados(
      @Parameter(description = "Período en formato YYYY/MM", example = "2025/01", required = true)
          @RequestParam
          String periodo,
      @Parameter(description = "ID de la seccional", required = true) @RequestParam
          Integer seccionalId,
      @Parameter(description = "Número de página (0-based)") @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
      @Parameter(description = "Si es true, retorna todos los registros sin paginación")
          @RequestParam(defaultValue = "false")
          boolean all) {

    try {
      logger.info(
          "Consultando descuentos procesados - Período: {}, Seccional: {}, Página: {}, Tamaño: {}, Todo: {}",
          periodo,
          seccionalId,
          page,
          size,
          all);

      if (all) {
        // Consultar todos los descuentos sin paginación
        List<DescuentoProcesado> descuentos =
            consultaDescuentosService.obtenerTodosDescuentosProcesados(periodo, seccionalId);
        String mensaje =
            String.format(
                "Se encontraron %d descuentos procesados para período %s y seccional %d",
                descuentos.size(), periodo, seccionalId);

        // Convertir a DTOs
        List<DescuentoProcesadoDTO> response = descuentoMapper.toDTOList(descuentos);

        return ResponseEntity.ok(ApiResponse.success(response, mensaje));
      } else {
        // Consultar con paginación
        org.springframework.data.domain.Pageable pageable =
            org.springframework.data.domain.PageRequest.of(page, size);

        org.springframework.data.domain.Page<DescuentoProcesado> descuentosPage =
            consultaDescuentosService.obtenerDescuentosProcesados(periodo, seccionalId, pageable);

        // Convertir a DTOs
        List<DescuentoProcesadoDTO> data = descuentoMapper.toDTOList(descuentosPage.getContent());

        // Crear información de paginación
        PaginationDTO pagination =
            new PaginationDTO(
                page + 1, // currentPage (1-based for frontend)
                descuentosPage.getTotalPages(),
                size,
                descuentosPage.getTotalElements());

        // Crear respuesta paginada
        PaginatedDescuentoProcesadoDTO response =
            new PaginatedDescuentoProcesadoDTO(data, pagination);

        String mensaje =
            String.format(
                "Página %d de %d - %d descuentos de %d totales para período %s y seccional %d",
                page + 1,
                descuentosPage.getTotalPages(),
                data.size(),
                descuentosPage.getTotalElements(),
                periodo,
                seccionalId);

        return ResponseEntity.ok(ApiResponse.success(response, mensaje));
      }

    } catch (IllegalArgumentException e) {
      logger.warn("Parámetros inválidos para consulta de descuentos: {}", e.getMessage());
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Parámetros inválidos: " + e.getMessage()));
    } catch (Exception e) {
      logger.error(
          "Error al consultar descuentos procesados - Período: {}, Seccional: {}",
          periodo,
          seccionalId,
          e);
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Error al consultar descuentos procesados: " + e.getMessage()));
    }
  }

  @GetMapping("/procesados/resumen")
  @Operation(
      summary = "Resumen de descuentos procesados",
      description =
          "Obtiene un resumen estadístico de los descuentos procesados por período y seccional")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR') or hasRole('CONSULTA')")
  public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerResumenDescuentosProcesados(
      @Parameter(description = "Período en formato YYYY/MM", example = "2025/01", required = true)
          @RequestParam
          String periodo,
      @Parameter(description = "ID de la seccional", required = true) @RequestParam
          Integer seccionalId) {

    try {
      logger.info(
          "Generando resumen de descuentos procesados - Período: {}, Seccional: {}",
          periodo,
          seccionalId);

      long totalDescuentos =
          consultaDescuentosService.contarDescuentosProcesados(periodo, seccionalId);

      Map<String, Object> resumen =
          Map.of(
              "periodo", periodo,
              "seccional_id", seccionalId,
              "total_descuentos", totalDescuentos,
              "fecha_consulta", java.time.LocalDateTime.now().toString());

      String mensaje =
          String.format(
              "Resumen generado: %d descuentos para período %s y seccional %d",
              totalDescuentos, periodo, seccionalId);

      return ResponseEntity.ok(ApiResponse.success(resumen, mensaje));

    } catch (IllegalArgumentException e) {
      logger.warn("Parámetros inválidos para resumen de descuentos: {}", e.getMessage());
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Parámetros inválidos: " + e.getMessage()));
    } catch (Exception e) {
      logger.error(
          "Error al generar resumen de descuentos - Período: {}, Seccional: {}",
          periodo,
          seccionalId,
          e);
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Error al generar resumen: " + e.getMessage()));
    }
  }

  @GetMapping("/cliente/{cedula}/procesados")
  @Operation(
      summary = "Consultar descuentos procesados por cédula",
      description = "Obtiene los descuentos procesados para un cliente específico por su cédula")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR') or hasRole('CONSULTA')")
  public ResponseEntity<ApiResponse<List<DescuentoClienteDTO>>> obtenerDescuentosPorCedula(
      @Parameter(description = "Cédula del cliente", required = true) @PathVariable String cedula) {

    try {
      logger.info("Consultando descuentos procesados para cliente con cédula: {}", cedula);

      List<DescuentoProcesado> descuentos =
          consultaDescuentosService.obtenerDescuentosPorCedula(cedula);

      List<DescuentoClienteDTO> response =
          descuentos.stream()
              .map(
                  d ->
                      new DescuentoClienteDTO(
                          d.getCedulaCliente(),
                          d.getPeriodo(),
                          d.getCodigoDescuento(),
                          d.getValor(),
                          d.getValorEsperado(),
                          d.getDiferencia(),
                          d.getEstado()))
              .toList();

      String mensaje =
          String.format(
              "Se encontraron %d descuentos procesados para el cliente con cédula %s",
              response.size(), cedula);

      return ResponseEntity.ok(ApiResponse.success(response, mensaje));

    } catch (IllegalArgumentException e) {
      logger.warn(
          "Parámetros inválidos para consulta de descuentos por cédula: {}", e.getMessage());
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Parámetros inválidos: " + e.getMessage()));
    } catch (Exception e) {
      logger.error(
          "Error al consultar descuentos procesados para cliente con cédula: {}", cedula, e);
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Error al consultar descuentos procesados: " + e.getMessage()));
    }
  }

  @GetMapping("/archivos/con-errores")
  @Operation(
      summary = "Obtener archivos con errores",
      description = "Lista los archivos que tienen errores registrados")
  @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR') or hasRole('CONSULTA')")
  public ResponseEntity<ApiResponse<List<ArchivoConErroresDTO>>> obtenerArchivosConErrores() {
    try {
      logger.debug("Consultando archivos con errores");

      List<ArchivoConErroresDTO> archivos = consultaService.obtenerArchivosConErrores();

      String mensaje =
          archivos.size() == 1
              ? "Se encontró 1 archivo con errores"
              : String.format("Se encontraron %d archivos con errores", archivos.size());

      return ResponseEntity.ok(ApiResponse.success(archivos, mensaje));

    } catch (Exception e) {
      logger.error("Error al consultar archivos con errores", e);
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Error al consultar archivos con errores: " + e.getMessage()));
    }
  }

  @GetMapping("/archivos/{archivoId}/debug-completo")
  @Operation(summary = "Debug completo del archivo")
  public ResponseEntity<Map<String, Object>> debugCompleto(@PathVariable Long archivoId) {

    try {
      // Consultar directamente desde BD
      EstadoProcesamiento estado = consultaService.obtenerEstadoProcesamiento(archivoId);

      Map<String, Object> debug =
          Map.of(
              "archivo_id", archivoId,
              "estado_dominio", estado.getEstado().name(),
              "estado_valor", estado.getEstado().getValor(),
              "en_procesamiento", consultaService.estaEnProcesamiento(archivoId),
              "total_lineas", estado.getTotalLineas(),
              "procesadas", estado.getLineasProcesadas(),
              "timestamp", java.time.LocalDateTime.now());

      return ResponseEntity.ok(debug);

    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }
}
