package com.example.demo.controller;

import com.example.demo.model.Cita;
import com.example.demo.service.CitaService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/citas")
@CrossOrigin(origins = "http://localhost:4200")
public class CitaController {

    @Autowired
    private CitaService service;

    @PostMapping
    public Cita save(@RequestBody Cita c) {
        return service.save(c);
    }

    @GetMapping
    public List<Cita> listar() {
        return service.listar();
    }

    @GetMapping("/fecha/{fecha}")
    public List<Cita> listarPorFecha(@PathVariable String fecha) {
        return service.listarPorFecha(java.time.LocalDate.parse(fecha));
    }

    @GetMapping("/odontologo/{idOdontologo}/fecha/{fecha}")
    public List<Cita> listarPorOdontologoYFecha(
            @PathVariable Integer idOdontologo,
            @PathVariable String fecha) {
        return service.listarPorOdontologoYFecha(idOdontologo, java.time.LocalDate.parse(fecha));
    }
}
