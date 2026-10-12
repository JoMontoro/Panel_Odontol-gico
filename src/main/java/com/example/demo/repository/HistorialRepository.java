package com.example.demo.repository;

import com.example.demo.model.HistorialClinico;
import com.example.demo.model.Paciente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HistorialRepository extends JpaRepository<HistorialClinico, Integer> {
    Optional<HistorialClinico> findByPaciente(Paciente paciente);

    /** Historiales de la cartera del odontologo conectado (-1 = ADMIN). */
    @Query("SELECT h FROM HistorialClinico h WHERE (:odoId = -1 or h.paciente.odontologo.id = :odoId)")
    List<HistorialClinico> listarDeCartera(@Param("odoId") Integer odoId);
}
