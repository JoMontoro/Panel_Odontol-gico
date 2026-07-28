package com.example.demo.repository;

import com.example.demo.model.CasoClinico;
import com.example.demo.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CasoClinicoRepository extends JpaRepository<CasoClinico, Integer> {
    List<CasoClinico> findByPacienteOrderByFechaInicioDesc(Paciente paciente);
    long countByEstado(com.example.demo.model.EstadoCaso estado);
}
