package conectaseguros.co.news_service.controller;

import conectaseguros.co.news_service.dto.ErrorProcesamientoDTO;
import conectaseguros.co.news_service.service.ErrorProcesamientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/news/errores-procesamiento")
@RequiredArgsConstructor
public class ErrorProcesamientoController {
    private final ErrorProcesamientoService service;

    @PostMapping
    public ResponseEntity<ErrorProcesamientoDTO> crear(@RequestBody ErrorProcesamientoDTO dto) {
        return ResponseEntity.ok(service.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ErrorProcesamientoDTO> actualizar(@PathVariable Long id, @RequestBody ErrorProcesamientoDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ErrorProcesamientoDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @GetMapping
    public ResponseEntity<List<ErrorProcesamientoDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }
}
