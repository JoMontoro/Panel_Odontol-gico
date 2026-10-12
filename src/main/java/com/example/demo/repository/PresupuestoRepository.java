package com.example.demo.repository;

import com.example.demo.model.CasoClinico;
import com.example.demo.model.EstadoPresupuesto;
import com.example.demo.model.Presupuesto;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PresupuestoRepository extends JpaRepository<Presupuesto, Integer> {
    Optional<Presupuesto> findFirstByCasoClinicoOrderByIdDesc(CasoClinico casoClinico);
    List<Presupuesto> findByCasoClinicoOrderByIdDesc(CasoClinico casoClinico);

    List<Presupuesto> findAllByOrderByIdDesc();

    /**
     * Verificaciones del odontologo conectado: -1 corresponde al ADMIN supervisor,
     * que ve los presupuestos de ambos.
     */
    @Query("SELECT pr FROM Presupuesto pr WHERE (:odoId = -1 or pr.casoClinico.paciente.odontologo.id = :odoId) "
            + "ORDER BY pr.id DESC")
    List<Presupuesto> listarDeCartera(@Param("odoId") Integer odoId);

    List<Presupuesto> findByEstadoOrderByIdDesc(EstadoPresupuesto estado);

    List<Presupuesto> findByCasoClinico_Paciente_Id(Integer pacienteId);

    List<Presupuesto> findByCasoClinico_Paciente_IdAndEstado(Integer pacienteId, EstadoPresupuesto estado);
}
