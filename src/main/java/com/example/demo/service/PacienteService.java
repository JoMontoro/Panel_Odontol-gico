
package com.example.demo.service;

import com.example.demo.model.Paciente;
import com.example.demo.repository.PacienteRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PacienteService {
    @Autowired
    private PacienteRepository repo;
    
    public Paciente save(Paciente p){
        return repo.save(p);
    }
    public List<Paciente> listar() {
        return repo.findAll();
    }
    
}
