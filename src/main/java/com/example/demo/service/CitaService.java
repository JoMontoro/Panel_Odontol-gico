package com.example.demo.service;

import com.example.demo.model.Cita;
import com.example.demo.model.CasoClinico;
import com.example.demo.model.EstadoCita;
import com.example.demo.model.Paciente;
import com.example.demo.model.PlanDetalle;
import com.example.demo.model.Usuario;
import com.example.demo.repository.CitaRepository;
import com.example.demo.repository.CasoClinicoRepository;
import com.example.demo.repository.PacienteRepository;
import com.example.demo.repository.PlanDetalleRepository;
import com.example.demo.model.Tratamiento;
import com.example.demo.repository.TratamientoRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CitaService {

    @Autowired
    private CitaRepository repo;

    @Autowired
    private CasoClinicoRepository casoRepo;

    @Autowired
    private PlanDetalleRepository planDetalleRepo;

    @Autowired
    private TratamientoRepository tratamientoRepo;

    @Autowired
    private SesionActual sesion;

    @Autowired
    private PacienteRepository pacienteRepo;

    /**
     * Agenda: el odontologo es siempre el usuario conectado, y el paciente/caso
     * tiene que ser de su cartera. Asi no se agenda a un paciente de otro colega.
     */
    public Cita save(Cita cita) {
        Usuario actual = sesion.exigirEscritura();
        Integer odoId = sesion.odontologoId();

        if (cita.getPlanDetalle() != null && cita.getPlanDetalle().getId() != null) {
            PlanDetalle planDetalle = planDetalleRepo.findById(cita.getPlanDetalle().getId())
                    .orElseThrow(() -> new RuntimeException("Detalle de plan no encontrado"));

            exigirPacientePropio(planDetalle.getPlan().getCasoClinico().getPaciente(), odoId);

            cita.setPlanDetalle(planDetalle);
            cita.setCasoClinico(planDetalle.getPlan().getCasoClinico());
            cita.setPaciente(planDetalle.getPlan().getCasoClinico().getPaciente());

            if (cita.getTratamiento() != null && cita.getTratamiento().getId() != null) {
                Tratamiento t = tratamientoRepo.findById(cita.getTratamiento().getId())
                        .orElseThrow(() -> new RuntimeException("Tratamiento no encontrado"));
                cita.setTratamiento(t);
            }
        } else {
            CasoClinico caso = casoRepo.findById(cita.getCasoClinico().getId())
                    .orElseThrow(() -> new RuntimeException("Caso clinico no encontrado"));
            exigirPacientePropio(caso.getPaciente(), odoId);
            cita.setCasoClinico(caso);
            cita.setPaciente(caso.getPaciente());
        }

        // La cita siempre queda a nombre del odontologo conectado.
        cita.setOdontologo(actual);

        if (cita.getEstado() == null) {
            cita.setEstado(EstadoCita.PROGRAMADA);
        }
        return repo.save(cita);
    }

    private void exigirPacientePropio(Paciente paciente, Integer odoId) {
        if (odoId == SesionActual.SIN_FILTRO) return;
        Integer dueno = paciente.getOdontologo() != null ? paciente.getOdontologo().getId() : null;
        if (dueno == null || !dueno.equals(odoId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Ese paciente pertenece a la cartera de otro odontologo.");
        }
    }

    /**
     * @param mostrarOcultas si es false, el listado excluye las citas canceladas,
     *                       las que el paciente no asistio y las cerradas por el pago
     *                       completo del presupuesto de tratamientos. Si se filtra por
     *                       un estado concreto, el filtro explicito tiene prioridad.
     */
    public Page<Cita> listarPaginado(int page, int size, String q, EstadoCita estado, boolean mostrarOcultas) {
        Integer odoId = sesion.odontologoId();
        String like = patronBusqueda(q);
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("fecha"), Sort.Order.asc("horaInicio")));

        if (mostrarOcultas || estado != null) {
            if (estado != null) {
                return repo.buscarPorEstadoYTermino(odoId, like, estado, pageable);
            }
            return repo.buscarPorTermino(odoId, like, pageable);
        }

        return repo.buscarVisiblesPorTermino(odoId, like, EstadoCita.CANCELADA, EstadoCita.NO_ASISTIO, pageable);
    }

    private static String patronBusqueda(String q) {
        if (q == null || q.trim().isEmpty()) return "%";
        return "%" + q.trim().toLowerCase() + "%";
    }

    public Optional<Cita> buscarPorId(Integer id) {
        Cita c = repo.findById(id).orElse(null);
        if (c == null) return Optional.empty();
        Integer odoId = sesion.odontologoId();
        if (odoId == SesionActual.SIN_FILTRO) return Optional.of(c);
        if (c.getOdontologo() == null || !odoId.equals(c.getOdontologo().getId())) {
            return Optional.empty();
        }
        return Optional.of(c);
    }

    public Cita actualizar(Integer id, Cita data) {
        Integer odoId = sesion.odontologoId();
        Cita cita = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));
        sesion.exigirDueño(cita.getOdontologo() != null ? cita.getOdontologo().getId() : null, "cita");

        if (data.getPlanDetalle() != null && data.getPlanDetalle().getId() != null) {
            PlanDetalle planDetalle = planDetalleRepo.findById(data.getPlanDetalle().getId())
                    .orElseThrow(() -> new RuntimeException("Detalle de plan no encontrado"));
            exigirPacientePropio(planDetalle.getPlan().getCasoClinico().getPaciente(), odoId);
            cita.setPlanDetalle(planDetalle);
            cita.setCasoClinico(planDetalle.getPlan().getCasoClinico());
            cita.setPaciente(planDetalle.getPlan().getCasoClinico().getPaciente());
        } else if (data.getCasoClinico() != null && data.getCasoClinico().getId() != null) {
            CasoClinico caso = casoRepo.findById(data.getCasoClinico().getId())
                    .orElseThrow(() -> new RuntimeException("Caso clinico no encontrado"));
            exigirPacientePropio(caso.getPaciente(), odoId);
            cita.setCasoClinico(caso);
            cita.setPaciente(caso.getPaciente());
        }
        if (data.getTratamiento() != null && data.getTratamiento().getId() != null) {
            Tratamiento t = tratamientoRepo.findById(data.getTratamiento().getId())
                    .orElseThrow(() -> new RuntimeException("Tratamiento no encontrado"));
            cita.setTratamiento(t);
        } else if (data.getTratamiento() == null) {
            // Si se envía null explícito, limpiar el tratamiento
            cita.setTratamiento(null);
        }
        if (data.getFecha() != null) cita.setFecha(data.getFecha());
        if (data.getHoraInicio() != null) cita.setHoraInicio(data.getHoraInicio());
        if (data.getHoraFin() != null) cita.setHoraFin(data.getHoraFin());
        if (data.getTipo() != null) cita.setTipo(data.getTipo());
        if (data.getEstado() != null) {
            cita.setEstado(data.getEstado());
            // Al reprogramar una cita cerrada por pago, vuelve al listado activo.
            if (data.getEstado() != EstadoCita.FINALIZADA) {
                cita.setCerradaPorPago(false);
            }
        }
        if (data.getObservaciones() != null) cita.setObservaciones(data.getObservaciones());
        return repo.save(cita);
    }

    public void cancelar(Integer id) {
        Cita cita = obtenerParaEscribir(id);
        cita.setEstado(com.example.demo.model.EstadoCita.CANCELADA);
        repo.save(cita);
    }

    public void eliminar(Integer id) {
        Cita cita = obtenerParaEscribir(id);
        try {
            repo.delete(cita);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new RuntimeException(
                    "No se puede eliminar: la cita tiene evoluciones u otros registros asociados. Cáncelala en su lugar.");
        }
    }

    private Cita obtenerParaEscribir(Integer id) {
        Cita cita = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));
        sesion.exigirDueño(cita.getOdontologo() != null ? cita.getOdontologo().getId() : null, "cita");
        return cita;
    }

    /** Agenda del dia: la del odontologo conectado (o la de ambos si es ADMIN). */
    public List<Cita> listarPorFecha(java.time.LocalDate fecha) {
        Integer odoId = sesion.odontologoId();
        if (odoId == SesionActual.SIN_FILTRO) {
            return repo.findByFechaOrderByHoraInicioAsc(fecha);
        }
        return repo.findByOdontologo_IdAndFechaOrderByHoraInicioAsc(odoId, fecha);
    }

    public List<Cita> listarPorOdontologoYFecha(Integer idOdontologo, java.time.LocalDate fecha) {
        Usuario actual = sesion.usuario();
        if (sesion.esAdmin(actual)) {
            com.example.demo.model.Usuario odontologo = new com.example.demo.model.Usuario();
            odontologo.setId(idOdontologo);
            return repo.findByOdontologoAndFechaOrderByHoraInicioAsc(odontologo, fecha);
        }
        // Un odontologo solo puede consultar su propia agenda.
        return repo.findByOdontologo_IdAndFechaOrderByHoraInicioAsc(actual.getId(), fecha);
    }

    /** Historial de citas de un paciente: primero se valida que sea de la cartera. */
    public List<Cita> listarPorPaciente(Integer pacienteId) {
        Integer odoId = sesion.odontologoId();
        if (odoId != SesionActual.SIN_FILTRO) {
            Paciente paciente = pacienteRepo.findById(pacienteId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));
            exigirPacientePropio(paciente, odoId);
        }
        return repo.findByPacienteIdOrderByFechaDescHoraInicioDesc(pacienteId);
    }
}
