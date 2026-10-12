package com.example.demo.controller;

import com.example.demo.dto.OdontogramaDTO;
import com.example.demo.service.OdontogramaService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/odontogramas")
@CrossOrigin(origins = "http://localhost:4200")
public class OdontogramaController {

    @Autowired
    private OdontogramaService service;

    /** Registra una nueva evaluación (dientes, tejidos blandos y "otros"). */
    @PostMapping
    public OdontogramaDTO crear(@RequestBody OdontogramaDTO dto) {
        return service.crear(dto);
    }

    /** Historial de odontogramas del paciente, del más reciente al más antiguo. */
    @GetMapping("/paciente/{idPaciente}")
    public List<OdontogramaDTO> listarPorPaciente(@PathVariable Integer idPaciente) {
        return service.listarPorPaciente(idPaciente);
    }

    @GetMapping("/{id}")
    public OdontogramaDTO obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @PutMapping("/{id}")
    public OdontogramaDTO actualizar(@PathVariable Integer id, @RequestBody OdontogramaDTO dto) {
        return service.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        service.eliminar(id);
    }
}
