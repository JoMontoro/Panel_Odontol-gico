package com.example.demo.controller;

import com.example.demo.dto.VerificacionDetalleDTO;
import com.example.demo.dto.VerificacionTratamientoDTO;
import com.example.demo.service.VerificacionTratamientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Apartado de verificacion de tratamientos: revisa que tratamientos estan
 * registrados en la base de datos, cuanto suman, cuanto pago el paciente y
 * permite editar el monto a cobrar.
 */
@RestController
@RequestMapping("/api/verificaciones")
@CrossOrigin(origins = "http://localhost:4200")
public class VerificacionTratamientoController {

    @Autowired
    private VerificacionTratamientoService service;

    @GetMapping
    public List<VerificacionTratamientoDTO> listar(@RequestParam(required = false) String q,
                                                   @RequestParam(required = false) String estado) {
        return service.listar(q, estado);
    }

    @GetMapping("/resumen")
    public Map<String, Object> resumen() {
        return service.resumen();
    }

    @GetMapping("/{presupuestoId}")
    public ResponseEntity<?> detalle(@PathVariable Integer presupuestoId) {
        try {
            return ResponseEntity.ok(service.detalle(presupuestoId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
