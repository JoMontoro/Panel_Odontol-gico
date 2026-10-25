package com.example.demo.service;

import com.example.demo.model.PlanDetalle;
import com.example.demo.model.PlanTratamiento;
import com.example.demo.model.Tratamiento;
import com.example.demo.model.EstadoDetalle;
import com.example.demo.repository.PlanDetalleRepository;
import com.example.demo.repository.PlanTratamientoRepository;
import com.example.demo.repository.TratamientoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PlanDetalleService {

    @Autowired
    private PlanDetalleRepository repo;

    @Autowired
    private PlanTratamientoRepository planRepo;

    @Autowired
    private TratamientoRepository tratamientoRepo;

    @Autowired
    private PresupuestoService presupuestoService;

    @Autowired
    private SesionActual sesion;

    private PlanTratamiento planDeCartera(Integer planId) {
        PlanTratamiento plan = planRepo.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan de tratamiento no encontrado"));
        if (!sesion.esDeMiCartera(plan.getCasoClinico() != null ? plan.getCasoClinico().getPaciente() : null)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Ese plan pertenece a la cartera de otro odontologo.");
        }
        return plan;
    }

    private PlanDetalle detalleDeCartera(Integer detalleId) {
        PlanDetalle detalle = repo.findById(detalleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Detalle de plan no encontrado"));
        if (!sesion.esDeMiCartera(
                detalle.getPlan() != null && detalle.getPlan().getCasoClinico() != null
                        ? detalle.getPlan().getCasoClinico().getPaciente() : null)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Ese tratamiento pertenece a la cartera de otro odontologo.");
        }
        return detalle;
    }

    public List<PlanDetalle> listarPorPlan(Integer planId) {
        return repo.findByPlanOrderByOrdenAsc(planDeCartera(planId));
    }

    /**
     * El plan solo puede usar tratamientos del catalogo propio: el catalogo de
     * cada odontologo es privado y no se comparte con el colega.
     */
    private void exigirTratamientoPropio(Tratamiento tratamiento) {
        if (sesion.esAdmin(sesion.usuario())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El rol ADMIN es solo de lectura: no puede modificar el plan de tratamiento.");
        }
        Integer odoId = sesion.odontologoId();
        if (tratamiento.getOdontologo() == null
                || !tratamiento.getOdontologo().getId().equals(odoId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El tratamiento \"" + tratamiento.getNombre() + "\" pertenece al catalogo de otro odontologo.");
        }
    }

    public List<PlanDetalle> listarPendientesPorPlan(Integer planId) {
        return repo.findByPlanAndEstado(planDeCartera(planId), EstadoDetalle.PENDIENTE);
    }

    public List<PlanDetalle> listarPendientesPorPaciente(Integer pacienteId) {
        sesion.exigirPacienteVisible(pacienteId);
        return repo.findByPlan_CasoClinico_Paciente_IdAndEstado(pacienteId, EstadoDetalle.PENDIENTE);
    }

    public Optional<PlanDetalle> buscarPorId(Integer id) {
        PlanDetalle detalle = repo.findById(id).orElse(null);
        if (detalle == null) return Optional.empty();
        boolean visible = sesion.esDeMiCartera(
                detalle.getPlan() != null && detalle.getPlan().getCasoClinico() != null
                        ? detalle.getPlan().getCasoClinico().getPaciente() : null);
        return visible ? Optional.of(detalle) : Optional.empty();
    }

    @Transactional
    public PlanDetalle agregarDetalle(Integer planId, Integer tratamientoId, Integer orden, Integer diente, String cara) {
        PlanTratamiento plan = planDeCartera(planId);
        sesion.exigirEscritura();

        Tratamiento tratamiento = tratamientoRepo.findById(tratamientoId)
                .orElseThrow(() -> new RuntimeException("Tratamiento no encontrado"));
        exigirTratamientoPropio(tratamiento);

        PlanDetalle detalle = new PlanDetalle();
        detalle.setPlan(plan);
        detalle.setTratamiento(tratamiento);
        detalle.setProcedimiento(tratamiento.getNombre());
        detalle.setOrden(orden);
        detalle.setPrecio(tratamiento.getPrecioReferencia());
        detalle.setDiente(diente);
        detalle.setCara(cara);
        detalle.setEstado(EstadoDetalle.PENDIENTE);

        PlanDetalle guardado = repo.save(detalle);
        presupuestoService.recalcularCaso(plan.getCasoClinico().getId());
        return guardado;
    }

    /**
     * Edicion completa de un tratamiento del plan, incluido el precio a cobrar al
     * paciente. Al cambiar el precio se recalcula el presupuesto del caso.
     */
    @Transactional
    public PlanDetalle actualizarDetalle(Integer planId, Integer detalleId, Map<String, Object> body) {
        PlanDetalle detalle = detalleDeCartera(detalleId);
        sesion.exigirEscritura();

        if (!detalle.getPlan().getId().equals(planId)) {
            throw new RuntimeException("El tratamiento no pertenece a este plan");
        }

        Integer casoId = detalle.getPlan().getCasoClinico().getId();

        if (body.containsKey("precio")) {
            Double precio = toDouble(body.get("precio"));
            if (precio != null && precio < 0) {
                throw new RuntimeException("El precio del tratamiento no puede ser negativo");
            }
            detalle.setPrecio(precio);
        }
        if (body.containsKey("procedimiento")) {
            detalle.setProcedimiento(toStringValue(body.get("procedimiento")));
        }
        if (body.containsKey("diente")) {
            detalle.setDiente(toInt(body.get("diente")));
        }
        if (body.containsKey("cara")) {
            detalle.setCara(toStringValue(body.get("cara")));
        }
        if (body.containsKey("orden")) {
            Integer orden = toInt(body.get("orden"));
            if (orden != null) detalle.setOrden(orden);
        }
        if (body.containsKey("tratamientoId")) {
            Integer tratamientoId = toInt(body.get("tratamientoId"));
            if (tratamientoId == null) {
                detalle.setTratamiento(null);
            } else {
                Tratamiento tratamiento = tratamientoRepo.findById(tratamientoId)
                        .orElseThrow(() -> new RuntimeException("Tratamiento no encontrado"));
                exigirTratamientoPropio(tratamiento);
                detalle.setTratamiento(tratamiento);
            }
        }
        if (body.containsKey("estado") && body.get("estado") != null) {
            detalle.setEstado(EstadoDetalle.valueOf(body.get("estado").toString()));
        }

        PlanDetalle guardado = repo.save(detalle);
        presupuestoService.recalcularCaso(casoId);
        return guardado;
    }

    @Transactional
    public PlanDetalle actualizarEstado(Integer detalleId, EstadoDetalle estado) {
        PlanDetalle detalle = detalleDeCartera(detalleId);
        sesion.exigirEscritura();
        detalle.setEstado(estado);
        PlanDetalle guardado = repo.save(detalle);
        presupuestoService.recalcularCaso(detalle.getPlan().getCasoClinico().getId());
        return guardado;
    }

    @Transactional
    public void eliminar(Integer detalleId) {
        PlanDetalle detalle = detalleDeCartera(detalleId);
        sesion.exigirEscritura();
        Integer casoId = detalle.getPlan().getCasoClinico().getId();
        repo.delete(detalle);
        presupuestoService.recalcularCaso(casoId);
    }

    private static Double toDouble(Object valor) {
        if (valor == null || valor.toString().isBlank()) return null;
        return Double.valueOf(valor.toString());
    }

    private static Integer toInt(Object valor) {
        if (valor == null || valor.toString().isBlank()) return null;
        return Integer.valueOf(valor.toString());
    }

    private static String toStringValue(Object valor) {
        return valor == null ? null : valor.toString();
    }
}