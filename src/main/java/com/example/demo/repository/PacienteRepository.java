package com.example.demo.repository;

import com.example.demo.model.EstadoAtencionPaciente;
import com.example.demo.model.Paciente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PacienteRepository extends JpaRepository<Paciente, Integer> {

    /**
     * Filtro de cartera: :odoId es el odontologo conectado y -1 cuando el usuario
     * es ADMIN (supervisor), en cuyo caso se ven los pacientes de todos.
     */
    String CARTERA = "(:odoId = -1 or p.odontologo.id = :odoId)";

    List<Paciente> findByEstadoTrue();

    /**
     * Pacientes con estado NULL (registros anteriores a la columna): se tratan
     * como activos para que nunca se pierdan del listado.
     */
    @Query("SELECT p FROM Paciente p WHERE p.estado is null")
    List<Paciente> findSinEstado();

    List<Paciente> findByOdontologoIsNull();

    List<Paciente> findByOdontologo_IdOrderByIdDesc(Integer odontologoId);

    long countByOdontologo_Id(Integer odontologoId);

    long countByEstadoAndOdontologo_Id(Boolean estado, Integer odontologoId);

    List<Paciente> findByEstadoAndOdontologo_IdOrderByIdDesc(Boolean estado, Integer odontologoId);

    Page<Paciente> findByEstadoTrue(Pageable pageable);

    Page<Paciente> findByEstado(Pageable pageable, Boolean estado);

    /**
     * Filtro de la lista de pacientes respetando el filtro activo/inactivo.
     * "Desactivar" nunca borra: solo pone estado = false, asi que el paciente
     * sigue registrado y reaparece con el filtro de mostrar ocultos.
     */
    @Query("SELECT p FROM Paciente p WHERE " + CARTERA +
            " and (:estado is null or coalesce(p.estado, true) = :estado)")
    Page<Paciente> listarPorEstado(@Param("estado") Boolean estado,
                                   @Param("odoId") Integer odoId,
                                   Pageable pageable);

    /**
     * Pacientes que todavia no cancelaron el total de sus tratamientos.
     * Incluye los registros legacy con estado_atencion NULL.
     */
    @Query("SELECT p FROM Paciente p WHERE " + CARTERA +
            " and (:estado is null or coalesce(p.estado, true) = :estado) " +
            "and (p.estadoAtencion is null or p.estadoAtencion <> :atendido)")
    Page<Paciente> listarNoAtendidos(@Param("estado") Boolean estado,
                                     @Param("odoId") Integer odoId,
                                     @Param("atendido") EstadoAtencionPaciente atendido,
                                     Pageable pageable);

    @Query("SELECT p FROM Paciente p WHERE " + CARTERA + " and p.estadoAtencion = :atendido")
    Page<Paciente> listarAtendidos(@Param("odoId") Integer odoId,
                                    @Param("atendido") EstadoAtencionPaciente atendido,
                                    Pageable pageable);
}
