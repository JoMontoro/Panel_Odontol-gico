package com.example.demo.repository;

import com.example.demo.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    /**
     * Suma de los abonos recibidos en un rango de fechas para la cartera del
     * odontologo conectado. -1 corresponde al ADMIN supervisor, que ve los
     * pagos de ambos odontologos.
     */
    @Query("SELECT COALESCE(SUM(pg.monto), 0.0) FROM Pago pg "
            + "WHERE (:odoId = -1 OR pg.presupuesto.casoClinico.paciente.odontologo.id = :odoId) "
            + "AND pg.fecha >= :desde AND pg.fecha < :hasta")
    Double sumarPorCarteraYRango(@Param("odoId") Integer odoId,
                                 @Param("desde") LocalDate desde,
                                 @Param("hasta") LocalDate hasta);

    List<Pago> findByPresupuesto_IdOrderByFechaDesc(Integer presupuestoId);
}
