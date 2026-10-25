package com.example.demo.service;

import com.example.demo.dto.RegistroPagoDTO;
import com.example.demo.model.CasoClinico;
import com.example.demo.model.Cita;
import com.example.demo.model.EstadoAtencionPaciente;
import com.example.demo.model.EstadoCaso;
import com.example.demo.model.EstadoCita;
import com.example.demo.model.EstadoDetalle;
import com.example.demo.model.EstadoPlan;
import com.example.demo.model.EstadoPresupuesto;
import com.example.demo.model.Paciente;
import com.example.demo.model.Pago;
import com.example.demo.model.PlanDetalle;
import com.example.demo.model.PlanTratamiento;
import com.example.demo.model.Presupuesto;
import com.example.demo.repository.CasoClinicoRepository;
import com.example.demo.repository.CitaRepository;
import com.example.demo.repository.PacienteRepository;
import com.example.demo.repository.PagoRepository;
import com.example.demo.repository.PlanDetalleRepository;
import com.example.demo.repository.PlanTratamientoRepository;
import com.example.demo.repository.PresupuestoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class PresupuestoService {

    private static final double EPSILON = 0.0001;

    private static final List<EstadoCita> ESTADOS_CITA_A_CERRAR = Arrays.asList(
            EstadoCita.PROGRAMADA, EstadoCita.CONFIRMADA, EstadoCita.EN_ATENCION);

    private static final List<EstadoPlan> ESTADOS_PLAN_A_CERRAR = Arrays.asList(
            EstadoPlan.PENDIENTE, EstadoPlan.EN_PROCESO);

    @Autowired
    private PresupuestoRepository presupuestoRepo;

    @Autowired
    private CasoClinicoRepository casoRepo;

    @Autowired
    private PlanTratamientoRepository planRepo;

    @Autowired
    private PlanDetalleRepository planDetalleRepo;

    @Autowired
    private CitaRepository citaRepo;

    @Autowired
    private PacienteRepository pacienteRepo;

    @Autowired
    private PagoRepository pagoRepo;

    @Autowired
    private SesionActual sesion;

    /** El presupuesto se lee y se modifica solo sobre un caso de la cartera propia. */
    private CasoClinico casoDeCartera(Integer casoId) {
        CasoClinico caso = casoRepo.findById(casoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caso clinico no encontrado"));
        if (!sesion.esDeMiCartera(caso.getPaciente())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Ese caso clinico pertenece a la cartera de otro odontologo.");
        }
        return caso;
    }

    private Presupuesto presupuestoDeCartera(Integer presupuestoId) {
        Presupuesto pre = presupuestoRepo.findById(presupuestoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Presupuesto no encontrado"));
        if (pre.getCasoClinico() == null || !sesion.esDeMiCartera(pre.getCasoClinico().getPaciente())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Ese presupuesto pertenece a la cartera de otro odontologo.");
        }
        return pre;
    }

    public Optional<Presupuesto> buscarPorCaso(Integer casoId) {
        return presupuestoRepo.findFirstByCasoClinicoOrderByIdDesc(casoDeCartera(casoId));
    }

    public List<Presupuesto> listarPorCaso(Integer casoId) {
        return presupuestoRepo.findByCasoClinicoOrderByIdDesc(casoDeCartera(casoId));
    }

    @Transactional
    public Presupuesto generar(Integer casoId, Double descuento) {
        CasoClinico caso = casoDeCartera(casoId);
        sesion.exigirEscritura();
        exigirTratamientosACotizar(caso);

        double subtotal = calcularSubtotal(caso);
        double desc = descuento != null && descuento > 0 ? descuento : 0.0;

        Optional<Presupuesto> existente = presupuestoRepo.findFirstByCasoClinicoOrderByIdDesc(caso);
        Presupuesto pre;

        if (existente.isPresent()
                && existente.get().getEstado() != EstadoPresupuesto.PAGADO
                && existente.get().getEstado() != EstadoPresupuesto.RECHAZADO) {
            pre = existente.get();
            pre.setSubtotal(subtotal);
            pre.setDescuento(desc);
            aplicarTotal(pre);
        } else {
            pre = new Presupuesto();
            pre.setCasoClinico(caso);
            pre.setSubtotal(subtotal);
            pre.setDescuento(desc);
            pre.setAdelanto(0.0);
            pre.setEstado(EstadoPresupuesto.PENDIENTE);
            aplicarTotal(pre);
        }

        return presupuestoRepo.save(pre);
    }

    /**
     * Permite al odontologo fijar el monto total a cobrar al paciente cuando el
     * importe calculado a partir de los tratamientos no es el monto final
     * (convenios, descuentos especiales, promociones, etc.).
     */
    @Transactional
    public RegistroPagoDTO actualizarMonto(Integer presupuestoId, Double monto) {
        if (monto == null || monto < 0) {
            throw new RuntimeException("El monto a cobrar no puede ser negativo");
        }

        Presupuesto pre = presupuestoDeCartera(presupuestoId);
        sesion.exigirEscritura();

        if (pre.getEstado() == EstadoPresupuesto.PAGADO) {
            throw new RuntimeException("Este presupuesto ya esta pagado. Ajuste el saldo mediante un reembolso.");
        }

        pre.setMontoPersonalizado(monto);
        double subtotal = pre.getSubtotal() != null ? pre.getSubtotal() : 0.0;
        pre.setDescuento(round2(Math.max(0, subtotal - monto)));
        return recalcularYSalvar(pre);
    }

    /**
     * Aplica un descuento sobre el subtotal del plan: el total a cobrar baja
     * automaticamente. El descuento nunca puede superar el subtotal, y si se
     * deja en 0 el presupuesto vuelve a la suma de los tratamientos.
     */
    @Transactional
    public RegistroPagoDTO actualizarDescuento(Integer presupuestoId, Double descuento) {
        if (descuento == null || descuento < 0) {
            throw new RuntimeException("El descuento no puede ser negativo");
        }

        Presupuesto pre = presupuestoDeCartera(presupuestoId);
        sesion.exigirEscritura();

        if (pre.getEstado() == EstadoPresupuesto.PAGADO) {
            throw new RuntimeException("Este presupuesto ya esta pagado. Ajuste el saldo mediante un reembolso.");
        }

        double subtotal = pre.getSubtotal() != null ? pre.getSubtotal() : 0.0;
        pre.setDescuento(round2(Math.min(descuento, subtotal)));
        pre.setMontoPersonalizado(null);
        return recalcularYSalvar(pre);
    }

    /**
     * Descarta el monto personalizado y vuelve al total calculado por los precios
     * de los tratamientos.
     */
    @Transactional
    public RegistroPagoDTO quitarMontoPersonalizado(Integer presupuestoId) {
        Presupuesto pre = presupuestoDeCartera(presupuestoId);
        sesion.exigirEscritura();

        if (pre.getEstado() == EstadoPresupuesto.PAGADO) {
            throw new RuntimeException("Este presupuesto ya esta pagado. Ajuste el saldo mediante un reembolso.");
        }

        pre.setMontoPersonalizado(null);
        pre.setDescuento(0.0);
        return recalcularYSalvar(pre);
    }

    /**
     * Recalcula el subtotal del presupuesto abierto de un caso a partir de los
     * precios actuales de sus tratamientos. Se invoca cuando se edita el precio
     * de un tratamiento del plan.
     */
    @Transactional
    public Presupuesto recalcularCaso(Integer casoId) {
        CasoClinico caso = casoDeCartera(casoId);
        Optional<Presupuesto> existente = presupuestoRepo.findFirstByCasoClinicoOrderByIdDesc(caso);
        if (existente.isEmpty()) return null;

        Presupuesto pre = existente.get();
        if (pre.getEstado() == EstadoPresupuesto.PAGADO
                || pre.getEstado() == EstadoPresupuesto.RECHAZADO) {
            return pre;
        }

        pre.setSubtotal(calcularSubtotal(caso));
        if (pre.getMontoPersonalizado() == null) {
            aplicarTotal(pre);
        } else {
            // El monto personalizado manda: solo se reajusta el saldo.
            pre.setSaldoPendiente(saldo(pre));
            pre.setEstado(EstadoPresupuesto.PENDIENTE);
        }
        return presupuestoRepo.save(pre);
    }

    @Transactional
    public Presupuesto aprobar(Integer presupuestoId) {
        Presupuesto pre = presupuestoDeCartera(presupuestoId);
        sesion.exigirEscritura();
        pre.setEstado(EstadoPresupuesto.APROBADO);
        return presupuestoRepo.save(pre);
    }

    @Transactional
    public RegistroPagoDTO registrarPago(Integer presupuestoId, Double monto) {
        if (monto == null || monto <= 0) {
            throw new RuntimeException("El monto debe ser mayor que 0");
        }

        Presupuesto pre = presupuestoDeCartera(presupuestoId);
        sesion.exigirEscritura();

        if (pre.getEstado() == EstadoPresupuesto.PAGADO) {
            throw new RuntimeException("Este presupuesto ya esta pagado. El pago fue cancelado en su totalidad.");
        }

        double saldo = pre.getSaldoPendiente() != null ? pre.getSaldoPendiente() : total(pre);

        if (monto - saldo > EPSILON) {
            throw new RuntimeException("El monto supera el saldo pendiente (S/ " + round2(saldo) + ")");
        }

        double adelanto = pre.getAdelanto() != null ? pre.getAdelanto() : 0.0;
        pre.setAdelanto(round2(adelanto + monto));

        boolean completaPago = monto - saldo >= -EPSILON;
        if (completaPago) {
            pre.setSaldoPendiente(0.0);
            pre.setEstado(EstadoPresupuesto.PAGADO);
        } else {
            pre.setSaldoPendiente(round2(saldo - monto));
            if (pre.getEstado() == EstadoPresupuesto.PENDIENTE) {
                pre.setEstado(EstadoPresupuesto.APROBADO);
            }
        }

        RegistroPagoDTO resultado = recalcularYSalvar(pre);

        Pago pago = new Pago();
        pago.setMonto(round2(monto));
        pago.setFecha(LocalDate.now());
        pago.setPresupuesto(resultado.getPresupuesto());
        pagoRepo.save(pago);

        return resultado;
    }

    /**
     * Guarda el presupuesto y, si el pago quedo completo, dispara en cascada:
     * tratamientos -> REALIZADO, plan -> COMPLETADO, citas -> FINALIZADA,
     * caso -> FINALIZADO y paciente -> ATENDIDO.
     */
    private RegistroPagoDTO recalcularYSalvar(Presupuesto pre) {
        Presupuesto guardado = presupuestoRepo.save(pre);

        if (guardado.getEstado() != EstadoPresupuesto.PAGADO || guardado.getCasoClinico() == null) {
            return new RegistroPagoDTO(guardado, false,
                    "Pago registrado. Saldo pendiente: S/ " + round2(guardado.getSaldoPendiente() != null
                            ? guardado.getSaldoPendiente() : 0.0),
                    0, 0, false, false);
        }

        CasoClinico caso = guardado.getCasoClinico();
        int citasFinalizadas = cerrarCitas(caso);
        int tratamientosRealizados = cerrarTratamientos(caso);
        boolean casoFinalizado = cerrarCaso(caso);
        boolean pacienteAtendido = marcarPacienteAtendido(caso);

        return new RegistroPagoDTO(guardado, true,
                "El pago ya fue cancelado en su totalidad.",
                citasFinalizadas, tratamientosRealizados, casoFinalizado, pacienteAtendido);
    }

    private int cerrarCitas(CasoClinico caso) {
        List<Cita> citas = citaRepo.findByCasoClinico_IdAndEstadoInOrderByFechaAscHoraInicioAsc(
                caso.getId(), ESTADOS_CITA_A_CERRAR);
        for (Cita cita : citas) {
            cita.setEstado(EstadoCita.FINALIZADA);
            cita.setCerradaPorPago(true);
        }
        if (!citas.isEmpty()) citaRepo.saveAll(citas);
        return citas.size();
    }

    private int cerrarTratamientos(CasoClinico caso) {
        int total = 0;
        for (PlanTratamiento plan : planRepo.findByCasoClinico_IdOrderByIdAsc(caso.getId())) {
            List<PlanDetalle> pendientes = planDetalleRepo.findByPlanAndEstado(plan, EstadoDetalle.PENDIENTE);
            for (PlanDetalle detalle : pendientes) {
                detalle.setEstado(EstadoDetalle.REALIZADO);
            }
            if (!pendientes.isEmpty()) planDetalleRepo.saveAll(pendientes);
            total += pendientes.size();

            if (ESTADOS_PLAN_A_CERRAR.contains(plan.getEstado())) {
                plan.setEstado(EstadoPlan.COMPLETADO);
                double costo = 0.0;
                for (PlanDetalle detalle : planDetalleRepo.findByPlanOrderByOrdenAsc(plan)) {
                    if (detalle.getEstado() != EstadoDetalle.CANCELADO && detalle.getPrecio() != null) {
                        costo += detalle.getPrecio();
                    }
                }
                if (costo > 0) {
                    plan.setCostoEstimado(round2(costo));
                }
                planRepo.save(plan);
            }
        }
        return total;
    }

    private boolean cerrarCaso(CasoClinico caso) {
        if (caso.getEstado() == EstadoCaso.FINALIZADO) return false;
        caso.setEstado(EstadoCaso.FINALIZADO);
        caso.setFechaFin(caso.getFechaFin() != null ? caso.getFechaFin() : LocalDate.now());
        casoRepo.save(caso);
        return true;
    }

    private boolean marcarPacienteAtendido(CasoClinico caso) {
        Paciente paciente = caso.getPaciente();
        if (paciente == null) return false;
        if (paciente.getEstadoAtencion() == EstadoAtencionPaciente.ATENDIDO) return false;

        // Solo se marca como atendido si no tiene otro caso con saldo pendiente.
        boolean otroCasoConSaldo = planRepo.findByCasoClinico_Paciente_Id(paciente.getId()).stream()
                .filter(p -> !p.getId().equals(caso.getId()))
                .anyMatch(p -> {
                    Optional<Presupuesto> otro =
                            presupuestoRepo.findFirstByCasoClinicoOrderByIdDesc(p.getCasoClinico());
                    if (otro.isEmpty()) return false;
                    Double saldo = otro.get().getSaldoPendiente();
                    return otro.get().getEstado() != EstadoPresupuesto.PAGADO
                            && saldo != null && saldo > EPSILON;
                });

        if (otroCasoConSaldo) return false;

        paciente.setEstadoAtencion(EstadoAtencionPaciente.ATENDIDO);
        pacienteRepo.save(paciente);
        return true;
    }

    private void aplicarTotal(Presupuesto pre) {
        if (pre.getMontoPersonalizado() != null) {
            pre.setTotal(round2(pre.getMontoPersonalizado()));
        } else {
            double subtotal = pre.getSubtotal() != null ? pre.getSubtotal() : 0.0;
            double desc = pre.getDescuento() != null ? pre.getDescuento() : 0.0;
            pre.setTotal(round2(Math.max(0, subtotal - desc)));
        }
        pre.setSaldoPendiente(round2(Math.max(0, saldo(pre))));
    }

    private double total(Presupuesto pre) {
        return pre.getTotal() != null ? pre.getTotal() : 0.0;
    }

    private double saldo(Presupuesto pre) {
        return total(pre) - (pre.getAdelanto() != null ? pre.getAdelanto() : 0.0);
    }

    private double calcularSubtotal(CasoClinico caso) {
        List<PlanTratamiento> planes = planRepo.findByCasoClinico(caso);
        double subtotal = 0.0;
        for (PlanTratamiento plan : planes) {
            if (plan.getEstado() == EstadoPlan.CANCELADO) continue;
            if (plan.getEstado() == EstadoPlan.COMPLETADO) continue;
            for (PlanDetalle d : planDetalleRepo.findByPlanOrderByOrdenAsc(plan)) {
                if (d.getEstado() == EstadoDetalle.CANCELADO) continue;
                if (d.getPrecio() != null) subtotal += d.getPrecio();
            }
        }
        return round2(subtotal);
    }

    /**
     * Un presupuesto generado en cero no sirve de nada: el odontologo creia que
     * el caso ya estaba cotizado. Antes se armaba vacio en silencio cuando el caso
     * no tenia plan de tratamiento, o cuando sus tratamientos no tenian precio.
     */
    private void exigirTratamientosACotizar(CasoClinico caso) {
        List<PlanTratamiento> planes = planRepo.findByCasoClinico(caso).stream()
                .filter(p -> p.getEstado() != EstadoPlan.CANCELADO && p.getEstado() != EstadoPlan.COMPLETADO)
                .collect(java.util.stream.Collectors.toList());

        if (planes.isEmpty()) {
            throw new RuntimeException(
                    "Este caso no tiene plan de tratamiento. Agregue los tratamientos del plan, "
                            + "con su precio, antes de generar el presupuesto.");
        }

        boolean algumConPrecio = planes.stream()
                .flatMap(p -> planDetalleRepo.findByPlanOrderByOrdenAsc(p).stream())
                .anyMatch(d -> d.getEstado() != EstadoDetalle.CANCELADO
                        && d.getPrecio() != null && d.getPrecio() > 0);

        if (!algumConPrecio) {
            throw new RuntimeException(
                    "Los tratamientos del plan de este caso no tienen precio asignado. "
                            + "Edite el precio de cada tratamiento del plan antes de generar el presupuesto.");
        }
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
