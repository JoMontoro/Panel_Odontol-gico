package com.example.demo.controller;

import com.example.demo.model.Tratamiento;
import com.example.demo.service.TratamientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tratamientos")
@CrossOrigin(origins = "http://localhost:4200")
public class TratamientoController {

    @Autowired
    private TratamientoService service;

    /** Solo los tratamientos activos: alimenta los selectores de citas y planes. */
    @GetMapping
    public List<Tratamiento> listarActivos() {
        return service.listarActivos();
    }

    /** Catalogo completo para la pantalla de administracion de tratamientos. */
    @GetMapping("/todos")
    public List<Tratamiento> listarTodos(@RequestParam(defaultValue = "true") boolean incluirInactivos) {
        return service.listarTodos(incluirInactivos);
    }

    @GetMapping("/categorias")
    public List<String> listarCategorias() {
        return service.listarCategorias();
    }

    @GetMapping("/categoria/{categoria}")
    public List<Tratamiento> listarPorCategoria(@PathVariable String categoria) {
        return service.listarPorCategoria(categoria);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tratamiento> obtener(@PathVariable Integer id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Tratamiento t) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(t));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody Tratamiento t) {
        try {
            return ResponseEntity.ok(service.actualizar(id, t));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Baja logica: conserva el historial pero deja de ofrecer el tratamiento. */
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Object activo = body.get("activo");
        boolean valor = activo != null && Boolean.parseBoolean(activo.toString());
        try {
            return ResponseEntity.ok(service.cambiarEstado(id, valor));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Eliminacion definitiva del registro en la base de datos. */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            service.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", e.getMessage()));
        }
    }
}
