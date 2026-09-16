package com.example.demo.repository;

import com.example.demo.model.CasoClinico;
import com.example.demo.model.EstadoPresupuesto;
import com.example.demo.model.Presupuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PresupuestoRepository extends JpaRepository<Presupuesto, Integer> {
    Optional<Presupuesto> findFirstByCasoClinicoOrderByIdDesc(CasoClinico casoClinico);
    List<Presupuesto> findByCasoClinicoOrderByIdDesc(CasoClinico casoClinico);
}