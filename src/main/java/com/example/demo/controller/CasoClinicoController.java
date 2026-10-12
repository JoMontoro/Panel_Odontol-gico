package com.example.demo.controller;

import com.example.demo.model.CasoClinico;
import com.example.demo.service.CasoClinicoService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class CasoClinicoController {

    @Autowired
    private CasoClinicoService service;

    @GetMapping("/pacientes/{pacienteId}/casos")
    public java.util.List<CasoClinico> listar(@PathVariable Integer pacienteId) {
        return service.listarPorPaciente(pacienteId);
    }

    @GetMapping("/casos/{id}")
    public ResponseEntity<CasoClinico> obtener(@PathVariable Integer id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/pacientes/{pacienteId}/casos")
    public CasoClinico crear(@PathVariable Integer pacienteId,
                             @RequestBody Map<String, String> body,
                             Authentication auth) {
        return service.crear(pacienteId, auth.getName(),
                body.get("motivoConsulta"), body.get("diagnostico"));
    }

    @PutMapping("/casos/{id}")
    public CasoClinico actualizar(@PathVariable Integer id,
                                  @RequestBody Map<String, String> body) {
        boolean vieneDiagnostico = body.containsKey("diagnostico");
        return service.actualizar(id, body.get("motivoConsulta"), body.get("estado"),
                body.get("diagnostico"), vieneDiagnostico);
    }

    @DeleteMapping("/casos/{id}")
    public void cancelar(@PathVariable Integer id) {
        service.cancelar(id);
    }
}
