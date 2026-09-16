package com.example.demo.controller;

import com.example.demo.model.Presupuesto;
import com.example.demo.service.PresupuestoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/presupuestos")
@CrossOrigin(origins = "http://localhost:4200")
public class PresupuestoController {

    @Autowired
    private PresupuestoService service;

    @GetMapping("/caso/{casoId}")
    public ResponseEntity<?> obtenerPorCaso(@PathVariable Integer casoId) {
        return service.buscarPorCaso(casoId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/caso/{casoId}/todos")
    public List<Presupuesto> listarPorCaso(@PathVariable Integer casoId) {
        return service.listarPorCaso(casoId);
    }

    @PostMapping("/caso/{casoId}")
    public Presupuesto generar(@PathVariable Integer casoId,
                               @RequestBody(required = false) Map<String, Object> body) {
        Double descuento = 0.0;
        if (body != null && body.get("descuento") != null) {
            descuento = Double.valueOf(body.get("descuento").toString());
        }
        return service.generar(casoId, descuento);
    }

    @PutMapping("/{presupuestoId}/aprobar")
    public Presupuesto aprobar(@PathVariable Integer presupuestoId) {
        return service.aprobar(presupuestoId);
    }

    @PostMapping("/{presupuestoId}/pagos")
    public Presupuesto registrarPago(@PathVariable Integer presupuestoId,
                                     @RequestBody Map<String, Object> body) {
        Double monto = body != null && body.get("monto") != null
                ? Double.valueOf(body.get("monto").toString()) : 0.0;
        return service.registrarPago(presupuestoId, monto);
    }
}