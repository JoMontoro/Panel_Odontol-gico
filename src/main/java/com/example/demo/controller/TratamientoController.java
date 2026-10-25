package com.example.demo.controller;

import com.example.demo.model.Tratamiento;
import com.example.demo.service.TratamientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tratamientos")
@CrossOrigin(origins = "http://localhost:4200")
public class TratamientoController {

    @Autowired
    private TratamientoService service;

    /** Solo los tratamientos activos propios: alimentan los selectores de citas y planes. */
    @GetMapping
    public List<Tratamiento> listarActivos() {
        return service.listarActivos();
    }

    /** Catalogo propio (o el de ambos si el usuario es el ADMIN supervisor). */
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
    public ResponseEntity<Tratamiento> crear(@RequestBody Tratamiento t) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(t));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tratamiento> actualizar(@PathVariable Integer id, @RequestBody Tratamiento t) {
        return ResponseEntity.ok(service.actualizar(id, t));
    }

    /** Baja logica: conserva el historial pero deja de ofrecer el tratamiento. */
    @PutMapping("/{id}/estado")
    public ResponseEntity<Tratamiento> cambiarEstado(@PathVariable Integer id, @RequestBody Tratamiento body) {
        return ResponseEntity.ok(service.cambiarEstado(id, Boolean.TRUE.equals(body.getActivo())));
    }

    /** Eliminacion definitiva del registro en la base de datos. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
