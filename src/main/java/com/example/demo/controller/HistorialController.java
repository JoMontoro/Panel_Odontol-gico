package com.example.demo.controller;

import com.example.demo.model.HistorialClinico;
import com.example.demo.model.Paciente;
import com.example.demo.repository.HistorialRepository;
import com.example.demo.service.SesionActual;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/historial")
@CrossOrigin(origins = "http://localhost:4200")
public class HistorialController {

    @Autowired
    private HistorialRepository repo;

    @Autowired
    private SesionActual sesion;

    @PostMapping
    public HistorialClinico save(@RequestBody HistorialClinico h) {
        sesion.exigirEscritura();
        if (h.getPaciente() == null || h.getPaciente().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El historial debe pertenece a un paciente");
        }
        Paciente paciente = sesion.exigirPacienteVisible(h.getPaciente().getId());
        h.setPaciente(paciente);
        return repo.save(h);
    }

    /** Solo los historiales de los pacientes de la cartera del odontologo. */
    @GetMapping
    public List<HistorialClinico> listar() {
        return repo.listarDeCartera(sesion.odontologoId());
    }

    @GetMapping("/paciente/{idPaciente}")
    public HistorialClinico obtenerPorPaciente(@PathVariable Integer idPaciente) {
        Paciente paciente = sesion.exigirPacienteVisible(idPaciente);
        return repo.findByPaciente(paciente).orElse(null);
    }

    @PutMapping("/{id}")
    public HistorialClinico actualizar(@PathVariable Integer id, @RequestBody HistorialClinico h) {
        HistorialClinico existente = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Historial no encontrado"));
        sesion.exigirPacienteDeCartera(existente.getPaciente());
        existente.setAntecedentesMedicos(h.getAntecedentesMedicos());
        existente.setAlergias(h.getAlergias());
        existente.setEnfermedades(h.getEnfermedades());
        existente.setObservacionesGenerales(h.getObservacionesGenerales());
        return repo.save(existente);
    }
}
