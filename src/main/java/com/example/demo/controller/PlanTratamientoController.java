package com.example.demo.controller;

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

    @PutMapping("/{planId}/estado")
    public PlanTratamiento actualizarEstado(@PathVariable Integer planId,
                                             @RequestBody Map<String, String> body) {
        EstadoPlan estado = EstadoPlan.valueOf(body.get("estado"));
        return service.actualizarEstado(planId, estado);
    }
}