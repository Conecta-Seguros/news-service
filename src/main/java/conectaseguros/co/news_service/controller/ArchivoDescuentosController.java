package conectaseguros.co.news_service.controller;

import conectaseguros.co.news_service.dto.ArchivoDescuentosDTO;
import conectaseguros.co.news_service.service.ArchivoDescuentosService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/news/archivos-descuentos")
@RequiredArgsConstructor
public class ArchivoDescuentosController {
    private final ArchivoDescuentosService service;

    @PostMapping
    public ResponseEntity<ArchivoDescuentosDTO> crear(@RequestBody ArchivoDescuentosDTO dto) {
        return ResponseEntity.ok(service.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArchivoDescuentosDTO> actualizar(@PathVariable Long id, @RequestBody ArchivoDescuentosDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArchivoDescuentosDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @GetMapping
    public ResponseEntity<List<ArchivoDescuentosDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }
}
