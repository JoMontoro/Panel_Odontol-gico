package com.example.demo.repository;

import com.example.demo.model.ControlEvolucion;
import com.example.demo.model.CasoClinico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ControlEvolucionRepository extends JpaRepository<ControlEvolucion, Integer> {
    List<ControlEvolucion> findByCasoClinicoOrderByFechaAsc(CasoClinico casoClinico);
}
