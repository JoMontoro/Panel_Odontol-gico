
package com.example.demo.controller;

import com.example.demo.model.Paciente;
import com.example.demo.service.PacienteService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/pacientes")
@CrossOrigin(origins = "http://localhost:4200")
public class PacienteController {
    @Autowired
    private PacienteService service;

 // 🔹 CREAR
    @PostMapping
    public Paciente save(@RequestBody Paciente p){
        return service.save(p);
    }

    // 🔹 LISTAR
    @GetMapping
    public List<Paciente> listar(){
        return service.listar();
    }

    // 🔹 OBTENER POR ID
    @GetMapping("/{id}")
    public Paciente obtener(@PathVariable Integer id){
        return service.buscarPorId(id).orElseThrow();
    }

    // 🔹 ACTUALIZAR
    @PutMapping("/{id}")
    public Paciente actualizar(@PathVariable Integer id, @RequestBody Paciente p){
        Paciente existente = service.buscarPorId(id)
        .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

    existente.setNombre(p.getNombre());
    existente.setApellido(p.getApellido());
    existente.setEdad(p.getEdad());
    existente.setApoderado(p.getApoderado());
    existente.setDireccion(p.getDireccion());
    existente.setDni(p.getDni());
    existente.setTelefono(p.getTelefono());
    existente.setEmail(p.getEmail());

    return service.save(existente);
    }

    // 🔹 ELIMINAR
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id){
        service.eliminar(id);
    }
}
