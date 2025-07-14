package conectaseguros.co.news_service.controller;

import conectaseguros.co.news_service.dto.ArchivoNovedadesDTO;
import conectaseguros.co.news_service.service.ArchivoNovedadesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/news/archivos-novedades")
@RequiredArgsConstructor
public class ArchivoNovedadesController {
    private final ArchivoNovedadesService service;

    @PostMapping
    public ResponseEntity<ArchivoNovedadesDTO> crear(@RequestBody ArchivoNovedadesDTO dto) {
        return ResponseEntity.ok(service.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArchivoNovedadesDTO> actualizar(@PathVariable Long id, @RequestBody ArchivoNovedadesDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArchivoNovedadesDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @GetMapping
    public ResponseEntity<List<ArchivoNovedadesDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }
}
