package com.example.demo.controller;

import com.example.demo.model.Paciente;
import com.example.demo.service.PacienteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pacientes")
@CrossOrigin(origins = "http://localhost:4200")
public class PacienteController {

    @Autowired
    private PacienteService service;

    // 🔹 CREAR
    @PostMapping
    public Paciente save( @RequestBody Paciente p){

        return service.save(p);

    }

    // 🔹 LISTAR
    @GetMapping
    public Page<Paciente> listar(@RequestParam(defaultValue = "0")int page,
                                 @RequestParam(defaultValue = "10")int size){

        return service.listarPaginado(page, size);

    }

    // 🔹 OBTENER POR ID
    @GetMapping("/{id}")
    public Paciente obtener(
            @PathVariable Integer id
    ){

        return service
                .buscarPorId(id)
                .orElseThrow();

    }

    // 🔥 DETALLE COMPLETO
    @GetMapping("/{id}/detalle")
    public ResponseEntity<?> obtenerDetalle(
            @PathVariable Integer id
    ) {

        return ResponseEntity.ok(
                service.obtenerDetalle(id)
        );

    }

    // 🔹 ACTUALIZAR
    @PutMapping("/{id}")
    public Paciente actualizar(
            @PathVariable Integer id,
            @Validated @RequestBody Paciente p
    ){

        Paciente existente =
                service.buscarPorId(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Paciente no encontrado"
                                )
                        );

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
    public void eliminar(
            @PathVariable Integer id
    ){

        service.eliminar(id);

    }

}