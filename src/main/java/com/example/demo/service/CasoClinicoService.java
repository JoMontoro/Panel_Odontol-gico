package com.example.demo.service;

import com.example.demo.model.CasoClinico;
import com.example.demo.model.Paciente;
import com.example.demo.model.Usuario;
import com.example.demo.repository.CasoClinicoRepository;
import com.example.demo.repository.PacienteRepository;
import com.example.demo.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CasoClinicoService {

    @Autowired
    private CasoClinicoRepository repo;

    @Autowired
    private PacienteRepository pacienteRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    public List<CasoClinico> listarPorPaciente(Integer pacienteId) {
        Paciente paciente = pacienteRepo.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        return repo.findByPacienteOrderByFechaInicioDesc(paciente);
    }

    public Optional<CasoClinico> buscarPorId(Integer id) {
        return repo.findById(id);
    }

    public CasoClinico crear(Integer pacienteId, String username, String motivoConsulta) {
        Paciente paciente = pacienteRepo.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        Usuario odontologo = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Odontólogo no encontrado"));

        CasoClinico caso = new CasoClinico();
        caso.setPaciente(paciente);
        caso.setOdontologoResponsable(odontologo);
        caso.setMotivoConsulta(motivoConsulta);
        return repo.save(caso);
    }

    public CasoClinico actualizar(Integer id, String motivoConsulta, String estado) {
        CasoClinico caso = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Caso clínico no encontrado"));
        if (motivoConsulta != null && !motivoConsulta.isBlank())
            caso.setMotivoConsulta(motivoConsulta);
        if (estado != null && !estado.isBlank())
            caso.setEstado(com.example.demo.model.EstadoCaso.valueOf(estado));
        return repo.save(caso);
    }

    public void cancelar(Integer id) {
        CasoClinico caso = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Caso clínico no encontrado"));
        caso.setEstado(com.example.demo.model.EstadoCaso.CANCELADO);
        repo.save(caso);
    }
}
