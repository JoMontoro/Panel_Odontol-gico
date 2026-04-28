
package com.example.demo.service;

import com.example.demo.model.Cita;
import com.example.demo.repository.CitaRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;


public class CitaService {
     @Autowired
    private CitaRepository repo;

    public Cita save(Cita cita){
        // lógica básica: evitar duplicidad luego
        return repo.save(cita);
    }

    public List<Cita> listar(){
        return repo.findAll();
    }
}
