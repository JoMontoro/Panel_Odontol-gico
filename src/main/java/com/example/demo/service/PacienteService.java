
package com.example.demo.service;

import com.example.demo.model.Paciente;
import com.example.demo.repository.PacienteRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PacienteService {
    @Autowired
    private PacienteRepository repo;
    
    // 🔹 CREAR
    public Paciente save(Paciente p){
        return repo.save(p);
    }

    // 🔹 LISTAR
    public List<Paciente> listar() {
        return repo.findAll();
    }

    // 🔹 BUSCAR POR ID
    public Optional<Paciente> buscarPorId(Integer id){
        return repo.findById(id);
    }

    // 🔹 ACTUALIZAR
    public Paciente actualizar(Integer id, Paciente p){
        Paciente existente = repo.findById(id).orElseThrow();

        existente.setNombre(p.getNombre());
        existente.setApellido(p.getApellido());
        existente.setEdad(p.getEdad());
        existente.setApoderado(p.getApoderado());
        existente.setDireccion(p.getDireccion());
        existente.setDni(p.getDni());
        existente.setTelefono(p.getTelefono());
        existente.setEmail(p.getEmail());

        return repo.save(existente);
    }

    // 🔹 ELIMINAR
    public void eliminar(Integer id){
        repo.deleteById(id);
    }
}
