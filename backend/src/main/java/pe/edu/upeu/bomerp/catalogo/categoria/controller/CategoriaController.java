package pe.edu.upeu.bomerp.catalogo.categoria.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.bomerp.catalogo.categoria.dto.CategoriaRequest;
import pe.edu.upeu.bomerp.catalogo.categoria.dto.CategoriaResponse;
import pe.edu.upeu.bomerp.catalogo.categoria.entity.Categoria;
import pe.edu.upeu.bomerp.catalogo.categoria.repository.CategoriaRepository;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;

import java.util.List;

@Tag(name = "Catálogo - Categorías", description = "CRUD de categorías independientes de productos (S07 BomERP)")
@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;

    @Operation(summary = "Listar todas las categorías")
    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listar() {
        List<CategoriaResponse> list = categoriaRepository.findAll().stream()
                .map(c -> new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion()))
                .toList();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Obtener categoría por ID")
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> obtener(@PathVariable Long id) {
        Categoria c = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
        return ResponseEntity.ok(new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion()));
    }

    @Operation(summary = "Crear nueva categoría")
    @PostMapping
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest request) {
        Categoria c = Categoria.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .build();
        Categoria saved = categoriaRepository.save(c);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CategoriaResponse(saved.getId(), saved.getNombre(), saved.getDescripcion()));
    }

    @Operation(summary = "Actualizar categoría existente")
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        Categoria c = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
        c.setNombre(request.nombre());
        c.setDescripcion(request.descripcion());
        Categoria updated = categoriaRepository.save(c);
        return ResponseEntity.ok(new CategoriaResponse(updated.getId(), updated.getNombre(), updated.getDescripcion()));
    }

    @Operation(summary = "Eliminar categoría por ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoría no encontrada con ID: " + id);
        }
        categoriaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
