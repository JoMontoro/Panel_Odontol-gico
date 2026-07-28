package com.example.demo.service;

import com.example.demo.model.Cita;
import com.example.demo.model.CasoClinico;
import com.example.demo.repository.CitaRepository;
import com.example.demo.repository.CasoClinicoRepository;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CitaService {

    @Autowired
    private CitaRepository repo;

    @Autowired
    private CasoClinicoRepository casoRepo;

    public Cita save(Cita cita) {
        CasoClinico caso = casoRepo.findById(cita.getCasoClinico().getId())
                .orElseThrow(() -> new RuntimeException("Caso clínico no encontrado"));
        cita.setCasoClinico(caso);
        cita.setPaciente(caso.getPaciente());
        return repo.save(cita);
    }

    public List<Cita> listar() {
        return repo.findAll();
    }

    public List<Cita> listarPorFecha(java.time.LocalDate fecha) {
        return repo.findByFechaOrderByHoraInicioAsc(fecha);
    }

    public List<Cita> listarPorOdontologoYFecha(Integer idOdontologo, java.time.LocalDate fecha) {
        com.example.demo.model.Usuario odontologo = new com.example.demo.model.Usuario();
        odontologo.setId(idOdontologo);
        return repo.findByOdontologoAndFechaOrderByHoraInicioAsc(odontologo, fecha);
    }
}
