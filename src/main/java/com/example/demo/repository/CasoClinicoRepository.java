package com.example.demo.repository;

import com.example.demo.model.CasoClinico;
import com.example.demo.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CasoClinicoRepository extends JpaRepository<CasoClinico, Integer> {

    /** El caso pertenece al odontologo que lleva al paciente. */
    String CARTERA = "(:odoId = -1 or c.paciente.odontologo.id = :odoId)";

    List<CasoClinico> findByPacienteOrderByFechaInicioDesc(Paciente paciente);

    List<CasoClinico> findByOdontologoResponsable_IdOrderByFechaInicioDesc(Integer odontologoId);

    long countByEstado(com.example.demo.model.EstadoCaso estado);

    long countByEstadoAndOdontologoResponsable_Id(com.example.demo.model.EstadoCaso estado,
                                                   Integer odontologoId);
}
