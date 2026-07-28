package com.example.demo.repository;

import com.example.demo.model.Cita;
import com.example.demo.model.Paciente;
import com.example.demo.model.Usuario;
import com.example.demo.model.EstadoCita;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CitaRepository extends JpaRepository<Cita, Integer> {

    long countByEstado(EstadoCita estado);

    List<Cita> findByFechaOrderByHoraInicioAsc(LocalDate fecha);

    List<Cita> findByOdontologoAndFechaOrderByHoraInicioAsc(Usuario odontologo, LocalDate fecha);

    Optional<Cita> findFirstByPacienteOrderByFechaDesc(Paciente paciente);
}
