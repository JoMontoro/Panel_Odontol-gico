
package com.example.demo.controller;

import com.example.demo.model.Paciente;
import com.example.demo.service.PacienteService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/paciente")
@CrossOrigin(origins = "http://localhost:4200")
public class PacienteController {
    @Autowired
    private PacienteService service;

    @PostMapping
    public Paciente save(@RequestBody Paciente p){
        return service.save(p);
    }

    @GetMapping
    public List<Paciente> listar(){
        return service.listar();
    }
}
