package com.example.demo.repository;

import com.example.demo.model.HistorialClinico;
import com.example.demo.model.Paciente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistorialRepository extends JpaRepository<HistorialClinico, Integer> {
    Optional<HistorialClinico> findByPaciente(Paciente paciente);
}
