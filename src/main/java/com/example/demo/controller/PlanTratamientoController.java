package com.example.demo.controller;

import com.example.demo.dto.GenerarPlanDTO;
import com.example.demo.model.PlanTratamiento;
import com.example.demo.model.EstadoPlan;
import com.example.demo.service.PlanTratamientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/planes-tratamiento")
@CrossOrigin(origins = "http://localhost:4200")
public class PlanTratamientoController {

    @Autowired
    private PlanTratamientoService service;

    @GetMapping("/caso/{casoId}")
    public List<PlanTratamiento> listarPorCaso(@PathVariable Integer casoId) {
        return service.listarPorCaso(casoId);
    }

    @GetMapping("/paciente/{pacienteId}/activos")
    public List<PlanTratamiento> listarActivosPorPaciente(@PathVariable Integer pacienteId) {
        return service.listarActivosPorPaciente(pacienteId);
    }

    @PostMapping("/caso/{casoId}")
    public PlanTratamiento crear(@PathVariable Integer casoId,
                                  @RequestBody Map<String, Object> body) {
        String nombre = (String) body.get("nombre");
        Integer sesionesEstimadas = (Integer) body.get("sesionesEstimadas");
        Double costoEstimado = body.get("costoEstimado") != null 
            ? Double.valueOf(body.get("costoEstimado").toString()) : null;
        return service.crear(casoId, nombre, sesionesEstimadas, costoEstimado);
    }

    /**
     * Genera el plan del caso con los tratamientos que elige el odontologo y
     * deja el presupuesto listo para generarse o editarse.
     */
    @PostMapping("/caso/{casoId}/generar")
    public ResponseEntity<?> generar(@PathVariable Integer casoId,
                                     @RequestBody GenerarPlanDTO body) {
        try {
            return ResponseEntity.ok(service.generarDesdeTratamientos(casoId, body));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    /**
     * Genera el plan del caso automaticamente desde el diagnostico y el motivo de
     * consulta. El odontologo puede editarlo despues: cambiar precios, quitar o
     * agregar tratamientos, y el presupuesto se recalcula automaticamente.
     */
    @PostMapping("/caso/{casoId}/automatico")
    public ResponseEntity<?> generarAutomatico(@PathVariable Integer casoId,
                                               @RequestParam(defaultValue = "false") boolean forzar) {
        try {
            return ResponseEntity.ok(service.generarDesdeDiagnostico(casoId, forzar));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/{planId}/estado")
    public PlanTratamiento actualizarEstado(@PathVariable Integer planId,
                                             @RequestBody Map<String, String> body) {
        EstadoPlan estado = EstadoPlan.valueOf(body.get("estado"));
        return service.actualizarEstado(planId, estado);
    }
}