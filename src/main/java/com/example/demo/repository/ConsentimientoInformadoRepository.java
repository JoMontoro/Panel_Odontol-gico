package com.example.demo.repository;

import com.example.demo.model.ConsentimientoInformado;
import com.example.demo.model.HistorialClinico;
import com.example.demo.model.Paciente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConsentimientoInformadoRepository extends JpaRepository<ConsentimientoInformado, Integer> {

    Optional<ConsentimientoInformado> findByPaciente(Paciente paciente);

    List<ConsentimientoInformado> findByPacienteOrderByFechaAceptacionDesc(Paciente paciente);

    Optional<ConsentimientoInformado> findByHistorialClinico(HistorialClinico historialClinico);

    /** Consentimientos de la cartera del odontologo conectado (-1 = ADMIN). */
    @Query("SELECT c FROM ConsentimientoInformado c WHERE (:odoId = -1 or c.paciente.odontologo.id = :odoId)")
    List<ConsentimientoInformado> listarDeCartera(@Param("odoId") Integer odoId);
}
