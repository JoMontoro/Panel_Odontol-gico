package com.example.demo.controller;

import com.example.demo.model.PlanDetalle;
import com.example.demo.model.EstadoDetalle;
import com.example.demo.service.PlanDetalleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/planes-tratamiento/{planId}/detalles")
@CrossOrigin(origins = "http://localhost:4200")
public class PlanDetalleController {

    @Autowired
    private PlanDetalleService service;

    @GetMapping
    public List<PlanDetalle> listarPorPlan(@PathVariable Integer planId) {
        return service.listarPorPlan(planId);
    }

    @GetMapping("/pendientes")
    public List<PlanDetalle> listarPendientesPorPlan(@PathVariable Integer planId) {
        return service.listarPendientesPorPlan(planId);
    }

    @PostMapping
    public PlanDetalle agregarDetalle(@PathVariable Integer planId,
                                       @RequestBody Map<String, Object> body) {
        Integer tratamientoId = toInt(body.get("tratamientoId"));
        Integer orden = toInt(body.get("orden"));
        Integer diente = toInt(body.get("diente"));
        String cara = (String) body.get("cara");
        return service.agregarDetalle(planId, tratamientoId, orden, diente, cara);
    }

    /** Edicion completa del tratamiento del plan, incluido el precio a cobrar. */
    @PutMapping("/{detalleId}")
    public ResponseEntity<?> actualizarDetalle(@PathVariable Integer planId,
                                               @PathVariable Integer detalleId,
                                               @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(service.actualizarDetalle(planId, detalleId, body));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/{detalleId}/estado")
    public PlanDetalle actualizarEstado(@PathVariable Integer planId,
                                         @PathVariable Integer detalleId,
                                         @RequestBody Map<String, String> body) {
        EstadoDetalle estado = EstadoDetalle.valueOf(body.get("estado"));
        return service.actualizarEstado(detalleId, estado);
    }

    @DeleteMapping("/{detalleId}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer planId,
                                          @PathVariable Integer detalleId) {
        service.eliminar(detalleId);
        return ResponseEntity.noContent().build();
    }

    private static Integer toInt(Object valor) {
        if (valor == null || valor.toString().isBlank()) return null;
        return Integer.valueOf(valor.toString());
    }
}
