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

    /**
     * Cada odontologo agenda para si: :odoId es el odontologo conectado y -1 para
     * el ADMIN supervisor, que ve la agenda de ambos.
     */
    String AGENDA = "(:odoId = -1 or c.odontologo.id = :odoId)";

    long countByEstado(EstadoCita estado);

    long countByEstadoAndOdontologo_Id(EstadoCita estado, Integer odontologoId);

    long countByOdontologo_IdAndFecha(Integer odontologoId, LocalDate fecha);

    long countByFecha(LocalDate fecha);

    List<Cita> findByFechaOrderByHoraInicioAsc(LocalDate fecha);

    List<Cita> findByOdontologoAndFechaOrderByHoraInicioAsc(Usuario odontologo, LocalDate fecha);

    Optional<Cita> findFirstByPacienteOrderByFechaDesc(Paciente paciente);

    List<Cita> findByPacienteIdOrderByFechaDescHoraInicioDesc(Integer pacienteId);

    List<Cita> findByOdontologo_IdAndFechaOrderByHoraInicioAsc(Integer odontologoId, LocalDate fecha);

    List<Cita> findByCasoClinico_IdOrderByFechaAscHoraInicioAsc(Integer casoId);

    List<Cita> findByCasoClinico_IdAndEstadoInOrderByFechaAscHoraInicioAsc(Integer casoId, List<EstadoCita> estados);

    boolean existsByTratamiento_Id(Integer tratamientoId);

    long countByCasoClinico_IdAndEstadoIn(Integer casoId, List<EstadoCita> estados);

    long countByOdontologo_IdAndFechaAndEstado(Integer odontologoId, LocalDate fecha, EstadoCita estado);

    /**
     * El patron de busqueda (like) se arma en el servicio como "%termino%" y se
     * compara contra el nombre del paciente, el motivo de consulta o el
     * odontologo responsable.
     */
    String CRITERIO_BUSQUEDA =
            "(lower(c.paciente.nombre) like :like " +
            " or lower(c.paciente.apellido) like :like " +
            " or lower(c.casoClinico.motivoConsulta) like :like " +
            " or lower(c.odontologo.nombre) like :like " +
            " or lower(c.odontologo.apellido) like :like)";

    @Query("SELECT c FROM Cita c WHERE " + AGENDA + " and " + CRITERIO_BUSQUEDA)
    Page<Cita> buscarPorTermino(@Param("odoId") Integer odoId,
                                @Param("like") String like,
                                Pageable pageable);

    @Query("SELECT c FROM Cita c WHERE " + AGENDA + " and c.estado = :estado and " + CRITERIO_BUSQUEDA)
    Page<Cita> buscarPorEstadoYTermino(@Param("odoId") Integer odoId,
                                       @Param("like") String like,
                                       @Param("estado") EstadoCita estado,
                                       Pageable pageable);

    /**
     * Citas visibles por defecto: descarta las canceladas / no asistidas y las que
     * se cerraron automaticamente porque el paciente completo el pago de sus
     * tratamientos (pasan al historial del caso).
     */
    @Query("SELECT c FROM Cita c WHERE " + AGENDA +
            " and c.estado <> :cancelada and c.estado <> :noAsistio " +
            "and (c.cerradaPorPago is null or c.cerradaPorPago = false) " +
            "and " + CRITERIO_BUSQUEDA)
    Page<Cita> buscarVisiblesPorTermino(@Param("odoId") Integer odoId,
                                        @Param("like") String like,
                                        @Param("cancelada") EstadoCita cancelada,
                                        @Param("noAsistio") EstadoCita noAsistio,
                                        Pageable pageable);
}
