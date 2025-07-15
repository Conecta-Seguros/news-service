package conectaseguros.co.news_service.controller;

import conectaseguros.co.news_service.dto.NovedadDTO;
import conectaseguros.co.news_service.dto.ProcesamientoArchivoDTO;
import conectaseguros.co.news_service.dto.ResultadoProcesamientoDTO;
import conectaseguros.co.news_service.service.NovedadService;
import conectaseguros.co.news_service.service.ProcesamientoNovedadesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/news/novedades")
@RequiredArgsConstructor
public class NovedadesController {

    private final NovedadService novedadService;
    private final ProcesamientoNovedadesService procesamientoService;

    /**
     * Historia de Usuario: Cargar archivo de novedades y recibir mensajes de éxito/error
     */
    @PostMapping("/procesar-archivo")
    public ResponseEntity<ResultadoProcesamientoDTO> procesarArchivoNovedades(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(value = "usuario", defaultValue = "ADMIN") String usuario) {

        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        if (!archivo.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Solo se permiten archivos CSV");
        }

        ResultadoProcesamientoDTO resultado = procesamientoService.procesarArchivoNovedades(archivo, usuario);
        return ResponseEntity.ok(resultado);
    }

    /**
     * Crear novedad individual
     */
    @PostMapping
    public ResponseEntity<NovedadDTO> crearNovedad(@RequestBody NovedadDTO dto) {
        return ResponseEntity.ok(novedadService.crear(dto));
    }

    /**
     * Actualizar novedad
     */
    @PutMapping("/{id}")
    public ResponseEntity<NovedadDTO> actualizarNovedad(@PathVariable Long id, @RequestBody NovedadDTO dto) {
        return ResponseEntity.ok(novedadService.actualizar(id, dto));
    }

    /**
     * Eliminar novedad
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNovedad(@PathVariable Long id) {
        novedadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtener novedad por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<NovedadDTO> obtenerNovedad(@PathVariable Long id) {
        return ResponseEntity.ok(novedadService.obtener(id));
    }

    /**
     * Listar todas las novedades
     */
    @GetMapping
    public ResponseEntity<List<NovedadDTO>> listarNovedades() {
        return ResponseEntity.ok(novedadService.listar());
    }

    /**
     * Listar novedades por cliente
     */
    @GetMapping("/cliente/{cedulaCliente}")
    public ResponseEntity<List<NovedadDTO>> listarNovedadesPorCliente(@PathVariable String cedulaCliente) {
        return ResponseEntity.ok(novedadService.listarPorCliente(cedulaCliente));
    }

    /**
     * Listar novedades por póliza
     */
    @GetMapping("/poliza/{numeroPoliza}")
    public ResponseEntity<List<NovedadDTO>> listarNovedadesPorPoliza(@PathVariable String numeroPoliza) {
        return ResponseEntity.ok(novedadService.listarPorPoliza(numeroPoliza));
    }

    /**
     * Obtener historial de procesamientos
     */
    @GetMapping("/historial-procesamientos")
    public ResponseEntity<List<ProcesamientoArchivoDTO>> obtenerHistorialProcesamientos() {
        return ResponseEntity.ok(procesamientoService.obtenerHistorialProcesamientos());
    }
}