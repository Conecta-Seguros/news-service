package conectaseguros.co.news_service.controller;

import conectaseguros.co.news_service.dto.DescuentoDTO;
import conectaseguros.co.news_service.service.DescuentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/news/descuentos")
@RequiredArgsConstructor
public class DescuentoController {
    private final DescuentoService service;

    @PostMapping
    public ResponseEntity<DescuentoDTO> crear(@RequestBody DescuentoDTO dto) {
        return ResponseEntity.ok(service.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DescuentoDTO> actualizar(@PathVariable Long id, @RequestBody DescuentoDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DescuentoDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @GetMapping
    public ResponseEntity<List<DescuentoDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }
}
