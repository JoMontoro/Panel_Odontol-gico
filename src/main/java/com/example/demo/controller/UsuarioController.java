package com.example.demo.controller;

import com.example.demo.model.Rol;
import com.example.demo.repository.UsuarioRepository;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    private final UsuarioRepository repo;

    public UsuarioController(UsuarioRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/odontologos")
    public List<Map<String, Object>> listarOdontologos() {
        return repo.findByRol(Rol.ODONTOLOGO).stream().map(u -> Map.<String, Object>of(
                "id", u.getId(),
                "nombre", u.getNombre(),
                "apellido", u.getApellido(),
                "especialidad", u.getEspecialidad() == null ? "" : u.getEspecialidad()
        )).toList();
    }
}