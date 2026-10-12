package com.example.demo.repository;

import com.example.demo.model.Odontograma;
import com.example.demo.model.Paciente;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OdontogramaRepository extends JpaRepository<Odontograma, Integer> {

    /** Evaluaciones del paciente, de la más reciente a la más antigua. */
    @Query("SELECT o FROM Odontograma o WHERE o.paciente = :paciente ORDER BY o.fechaRegistro DESC")
    List<Odontograma> findByPacienteOrderByFechaRegistroDesc(@Param("paciente") Paciente paciente);
}
