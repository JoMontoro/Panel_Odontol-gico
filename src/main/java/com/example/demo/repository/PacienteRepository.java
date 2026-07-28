package com.example.demo.repository;

import com.example.demo.model.Paciente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Integer> {
    Page<Paciente> findByEstadoTrue(Pageable pageable);
    Page<Paciente> findByEstado(Pageable pageable, Boolean estado);
    java.util.List<Paciente> findByEstadoTrue();
}
