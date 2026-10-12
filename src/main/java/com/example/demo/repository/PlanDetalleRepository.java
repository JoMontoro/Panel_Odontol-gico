package com.example.demo.repository;

import com.example.demo.model.CasoClinico;
import com.example.demo.model.PlanDetalle;
import com.example.demo.model.PlanTratamiento;
import com.example.demo.model.EstadoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlanDetalleRepository extends JpaRepository<PlanDetalle, Integer> {
    List<PlanDetalle> findByPlanOrderByOrdenAsc(PlanTratamiento plan);
    List<PlanDetalle> findByPlanAndEstado(PlanTratamiento plan, EstadoDetalle estado);
    List<PlanDetalle> findByPlan_CasoClinico_Paciente_IdAndEstado(Integer pacienteId, EstadoDetalle estado);

    List<PlanDetalle> findByPlan_CasoClinico(CasoClinico casoClinico);

    boolean existsByTratamiento_Id(Integer tratamientoId);

    long countByPlan_CasoClinico_IdAndEstado(Integer casoId, EstadoDetalle estado);
}
