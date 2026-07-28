package com.example.demo.controller;

import com.example.demo.model.HistorialClinico;
import com.example.demo.repository.HistorialRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/historial")
@CrossOrigin(origins = "http://localhost:4200")
public class HistorialController {

    @Autowired
    private HistorialRepository repo;

    @PostMapping
    public HistorialClinico save(@RequestBody HistorialClinico h) {
        return repo.save(h);
    }

    @GetMapping
    public List<HistorialClinico> listar() {
        return repo.findAll();
    }

    @GetMapping("/paciente/{idPaciente}")
    public HistorialClinico obtenerPorPaciente(@PathVariable Integer idPaciente) {
        com.example.demo.model.Paciente p = new com.example.demo.model.Paciente();
        p.setId(idPaciente);
        return repo.findByPaciente(p).orElse(null);
    }

    @PutMapping("/{id}")
    public HistorialClinico actualizar(@PathVariable Integer id, @RequestBody HistorialClinico h) {
        HistorialClinico existente = repo.findById(id).orElseThrow();
        existente.setAntecedentesMedicos(h.getAntecedentesMedicos());
        existente.setAlergias(h.getAlergias());
        existente.setEnfermedades(h.getEnfermedades());
        existente.setObservacionesGenerales(h.getObservacionesGenerales());
        return repo.save(existente);
    }
}
