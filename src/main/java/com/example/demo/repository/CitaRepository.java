package com.example.demo.repository;

import com.example.demo.model.Cita;
import com.example.demo.model.Paciente;
import com.example.demo.model.Usuario;
import com.example.demo.model.EstadoCita;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CitaRepository extends JpaRepository<Cita, Integer> {

    long countByEstado(EstadoCita estado);

    List<Cita> findByFechaOrderByHoraInicioAsc(LocalDate fecha);

    List<Cita> findByOdontologoAndFechaOrderByHoraInicioAsc(Usuario odontologo, LocalDate fecha);

    Optional<Cita> findFirstByPacienteOrderByFechaDesc(Paciente paciente);

    @Query("SELECT c FROM Cita c WHERE " +
            "(:q = '' OR LOWER(c.paciente.nombre) LIKE LOWER(CONCAT(CONCAT('%', :q), '%')) " +
            " OR LOWER(c.paciente.apellido) LIKE LOWER(CONCAT(CONCAT('%', :q), '%')) " +
            " OR LOWER(c.casoClinico.motivoConsulta) LIKE LOWER(CONCAT(CONCAT('%', :q), '%')) " +
            " OR LOWER(c.odontologo.nombre) LIKE LOWER(CONCAT(CONCAT('%', :q), '%')) " +
            " OR LOWER(c.odontologo.apellido) LIKE LOWER(CONCAT(CONCAT('%', :q), '%')))")
    Page<Cita> buscarPorTermino(@Param("q") String q, Pageable pageable);

    @Query("SELECT c FROM Cita c WHERE c.estado = :estado AND " +
            "(:q = '' OR LOWER(c.paciente.nombre) LIKE LOWER(CONCAT(CONCAT('%', :q), '%')) " +
            " OR LOWER(c.paciente.apellido) LIKE LOWER(CONCAT(CONCAT('%', :q), '%')) " +
            " OR LOWER(c.casoClinico.motivoConsulta) LIKE LOWER(CONCAT(CONCAT('%', :q), '%')) " +
            " OR LOWER(c.odontologo.nombre) LIKE LOWER(CONCAT(CONCAT('%', :q), '%')) " +
            " OR LOWER(c.odontologo.apellido) LIKE LOWER(CONCAT(CONCAT('%', :q), '%')))")
    Page<Cita> buscarPorEstadoYTermino(@Param("q") String q, @Param("estado") EstadoCita estado, Pageable pageable);
}
