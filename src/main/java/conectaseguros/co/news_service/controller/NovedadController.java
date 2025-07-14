package conectaseguros.co.news_service.controller;

import conectaseguros.co.news_service.dto.NovedadDTO;
import conectaseguros.co.news_service.service.NovedadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/news/novedades")
@RequiredArgsConstructor
public class NovedadController {
    private final NovedadService service;

    @PostMapping
    public ResponseEntity<NovedadDTO> crear(@RequestBody NovedadDTO dto) {
        return ResponseEntity.ok(service.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NovedadDTO> actualizar(@PathVariable Long id, @RequestBody NovedadDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<NovedadDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @GetMapping
    public ResponseEntity<List<NovedadDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }
}
