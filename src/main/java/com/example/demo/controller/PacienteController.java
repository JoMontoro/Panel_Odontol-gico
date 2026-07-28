package com.example.demo.controller;

import com.example.demo.model.Paciente;
import com.example.demo.service.PacienteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pacientes")
@CrossOrigin(origins = "http://localhost:4200")
public class PacienteController {

    @Autowired
    private PacienteService service;

    @PostMapping
    public Paciente save(@RequestBody Paciente p) {
        return service.save(p);
    }

    @GetMapping
    public Page<Paciente> listar(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) Boolean estado) {
        return service.listar(page, size, estado);
    }

    @GetMapping("/{id}")
    public Paciente obtener(@PathVariable Integer id) {
        return service.buscarPorId(id).orElseThrow();
    }

    @GetMapping("/{id}/detalle")
    public ResponseEntity<Map<String, Object>> obtenerDetalle(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtenerDetalle(id));
    }

    @PutMapping("/{id}")
    public Paciente actualizar(@PathVariable Integer id, @Validated @RequestBody Paciente p) {
        return service.actualizar(id, p);
    }

    @PutMapping("/{id}/reactivar")
    public void reactivar(@PathVariable Integer id) {
        service.reactivar(id);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        service.desactivar(id);
    }
}
