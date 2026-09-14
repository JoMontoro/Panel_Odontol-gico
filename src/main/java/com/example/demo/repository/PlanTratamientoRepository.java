package com.example.demo.repository;

import com.example.demo.model.PlanTratamiento;
import com.example.demo.model.CasoClinico;
import com.example.demo.model.Paciente;
import com.example.demo.model.EstadoPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PlanTratamientoRepository extends JpaRepository<PlanTratamiento, Integer> {
    Optional<PlanTratamiento> findByCasoClinicoAndEstadoNot(CasoClinico caso, EstadoPlan estado);
    List<PlanTratamiento> findByCasoClinico(CasoClinico caso);
    List<PlanTratamiento> findByCasoClinicoPacienteAndEstado(
            Paciente paciente,
            EstadoPlan estado
    );}