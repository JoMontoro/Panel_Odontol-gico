package com.example.demo.service;

import com.example.demo.model.PlanTratamiento;
import com.example.demo.model.CasoClinico;
import com.example.demo.model.Paciente;
import com.example.demo.model.EstadoPlan;
import com.example.demo.repository.PlanTratamientoRepository;
import com.example.demo.repository.CasoClinicoRepository;
import com.example.demo.repository.PacienteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PlanTratamientoService {

    @Autowired
    private PlanTratamientoRepository repo;

    @Autowired
    private CasoClinicoRepository casoRepo;

    @Autowired
    private PacienteRepository pacienteRepo;

    public List<PlanTratamiento> listarPorCaso(Integer casoId) {
        CasoClinico caso = casoRepo.findById(casoId)
                .orElseThrow(() -> new RuntimeException("Caso clínico no encontrado"));
        return repo.findByCasoClinico(caso);
    }

    public List<PlanTratamiento> listarActivosPorPaciente(Integer pacienteId) {
        Paciente paciente = pacienteRepo.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        return repo.findByCasoClinicoPacienteAndEstado(paciente, EstadoPlan.EN_PROCESO);
    }

    public Optional<PlanTratamiento> buscarActivoPorCaso(Integer casoId) {
        CasoClinico caso = casoRepo.findById(casoId)
                .orElseThrow(() -> new RuntimeException("Caso clínico no encontrado"));
        return repo.findByCasoClinicoAndEstadoNot(caso, EstadoPlan.CANCELADO);
    }

    @Transactional
    public PlanTratamiento crear(Integer casoId, String nombre, Integer sesionesEstimadas, Double costoEstimado) {
        CasoClinico caso = casoRepo.findById(casoId)
                .orElseThrow(() -> new RuntimeException("Caso clínico no encontrado"));

        PlanTratamiento plan = new PlanTratamiento();
        plan.setCasoClinico(caso);
        plan.setNombre(nombre);
        plan.setSesionesEstimadas(sesionesEstimadas);
        plan.setCostoEstimado(costoEstimado);
        plan.setEstado(EstadoPlan.PENDIENTE);

        return repo.save(plan);
    }

    @Transactional
    public PlanTratamiento actualizarEstado(Integer planId, EstadoPlan estado) {
        PlanTratamiento plan = repo.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan de tratamiento no encontrado"));
        plan.setEstado(estado);
        return repo.save(plan);
    }
}