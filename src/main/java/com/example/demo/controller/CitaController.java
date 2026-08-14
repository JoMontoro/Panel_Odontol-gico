package com.example.demo.controller;

import com.example.demo.model.Cita;
import com.example.demo.model.EstadoCita;
import com.example.demo.service.CitaService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/citas")
@CrossOrigin(origins = "http://localhost:4200")
public class CitaController {

    @Autowired
    private CitaService service;

    @PostMapping
    public Cita save(@RequestBody Cita c) {
        return service.save(c);
    }

    @GetMapping
    public Page<Cita> listar(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) String q,
                             @RequestParam(required = false) EstadoCita estado) {
        return service.listarPaginado(page, size, q, estado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtener(@PathVariable Integer id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Cita actualizar(@PathVariable Integer id, @RequestBody Cita c) {
        return service.actualizar(id, c);
    }

    @PutMapping("/{id}/cancelar")
    public void cancelar(@PathVariable Integer id) {
        service.cancelar(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            service.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", e.getMessage()));
        }
    }

    @GetMapping("/fecha/{fecha}")
    public List<Cita> listarPorFecha(@PathVariable String fecha) {
        return service.listarPorFecha(java.time.LocalDate.parse(fecha));
    }

    @GetMapping("/odontologo/{idOdontologo}/fecha/{fecha}")
    public List<Cita> listarPorOdontologoYFecha(
            @PathVariable Integer idOdontologo,
            @PathVariable String fecha) {
        return service.listarPorOdontologoYFecha(idOdontologo, java.time.LocalDate.parse(fecha));
    }
}
