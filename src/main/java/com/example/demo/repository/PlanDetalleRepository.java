package com.example.demo.repository;

import com.example.demo.model.PlanDetalle;
import com.example.demo.model.PlanTratamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlanDetalleRepository extends JpaRepository<PlanDetalle, Integer> {
    List<PlanDetalle> findByPlanOrderByOrdenAsc(PlanTratamiento plan);
}
