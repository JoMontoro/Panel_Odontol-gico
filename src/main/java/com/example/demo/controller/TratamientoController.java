package com.example.demo.controller;

import com.example.demo.model.Tratamiento;
import com.example.demo.repository.TratamientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tratamientos")
@CrossOrigin(origins = "http://localhost:4200")
public class TratamientoController {

    @Autowired
    private TratamientoRepository repo;

    @GetMapping
    public List<Tratamiento> listarTodos() {
        return repo.findByActivoTrue();
    }

    @GetMapping("/categoria/{categoria}")
    public List<Tratamiento> listarPorCategoria(@PathVariable String categoria) {
        return repo.findByActivoTrueAndCategoria(categoria);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tratamiento> obtener(@PathVariable Integer id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Tratamiento crear(@RequestBody Tratamiento t) {
        return repo.save(t);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tratamiento> actualizar(@PathVariable Integer id,
                                                   @RequestBody Tratamiento t) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setCodigo(t.getCodigo());
                    existing.setNombre(t.getNombre());
                    existing.setDescripcion(t.getDescripcion());
                    existing.setDuracionMinutos(t.getDuracionMinutos());
                    existing.setPrecioReferencia(t.getPrecioReferencia());
                    existing.setCategoria(t.getCategoria());
                    existing.setActivo(t.getActivo());
                    return ResponseEntity.ok(repo.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id) {
        return repo.findById(id)
                .map(t -> {
                    t.setActivo(false);
                    repo.save(t);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}