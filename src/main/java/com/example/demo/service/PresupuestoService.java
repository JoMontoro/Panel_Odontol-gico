package com.example.demo.service;

import com.example.demo.model.CasoClinico;
import com.example.demo.model.EstadoDetalle;
import com.example.demo.model.EstadoPlan;
import com.example.demo.model.EstadoPresupuesto;
import com.example.demo.model.PlanDetalle;
import com.example.demo.model.PlanTratamiento;
import com.example.demo.model.Presupuesto;
import com.example.demo.repository.CasoClinicoRepository;
import com.example.demo.repository.PlanDetalleRepository;
import com.example.demo.repository.PlanTratamientoRepository;
import com.example.demo.repository.PresupuestoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PresupuestoService {

    @Autowired
    private PresupuestoRepository presupuestoRepo;

    @Autowired
    private CasoClinicoRepository casoRepo;

    @Autowired
    private PlanTratamientoRepository planRepo;

    @Autowired
    private PlanDetalleRepository planDetalleRepo;

    public Optional<Presupuesto> buscarPorCaso(Integer casoId) {
        CasoClinico caso = casoRepo.findById(casoId)
                .orElseThrow(() -> new RuntimeException("Caso clínico no encontrado"));
        return presupuestoRepo.findFirstByCasoClinicoOrderByIdDesc(caso);
    }

    public List<Presupuesto> listarPorCaso(Integer casoId) {
        CasoClinico caso = casoRepo.findById(casoId)
                .orElseThrow(() -> new RuntimeException("Caso clínico no encontrado"));
        return presupuestoRepo.findByCasoClinicoOrderByIdDesc(caso);
    }

    @Transactional
    public Presupuesto generar(Integer casoId, Double descuento) {
        CasoClinico caso = casoRepo.findById(casoId)
                .orElseThrow(() -> new RuntimeException("Caso clínico no encontrado"));

        double subtotal = calcularSubtotal(caso);
        double desc = descuento != null && descuento > 0 ? descuento : 0.0;
        double total = Math.max(0, subtotal - desc);

        Optional<Presupuesto> existente = presupuestoRepo.findFirstByCasoClinicoOrderByIdDesc(caso);
        Presupuesto pre;

        if (existente.isPresent()
                && existente.get().getEstado() != EstadoPresupuesto.PAGADO
                && existente.get().getEstado() != EstadoPresupuesto.RECHAZADO) {
            pre = existente.get();
            pre.setSubtotal(subtotal);
            pre.setDescuento(desc);
            pre.setTotal(total);
            double adelanto = pre.getAdelanto() != null ? pre.getAdelanto() : 0.0;
            pre.setSaldoPendiente(Math.max(0, total - adelanto));
            if (pre.getSaldoPendiente() <= 0) {
                pre.setSaldoPendiente(0.0);
                pre.setEstado(EstadoPresupuesto.PAGADO);
            }
        } else {
            pre = new Presupuesto();
            pre.setCasoClinico(caso);
            pre.setSubtotal(subtotal);
            pre.setDescuento(desc);
            pre.setTotal(total);
            pre.setAdelanto(0.0);
            pre.setSaldoPendiente(total);
            pre.setEstado(EstadoPresupuesto.PENDIENTE);
        }

        return presupuestoRepo.save(pre);
    }

    @Transactional
    public Presupuesto aprobar(Integer presupuestoId) {
        Presupuesto pre = presupuestoRepo.findById(presupuestoId)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));
        pre.setEstado(EstadoPresupuesto.APROBADO);
        return presupuestoRepo.save(pre);
    }

    @Transactional
    public Presupuesto registrarPago(Integer presupuestoId, Double monto) {
        if (monto == null || monto <= 0) {
            throw new RuntimeException("El monto debe ser mayor que 0");
        }

        Presupuesto pre = presupuestoRepo.findById(presupuestoId)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

        if (pre.getEstado() == EstadoPresupuesto.PAGADO) {
            throw new RuntimeException("Este presupuesto ya está pagado");
        }

        double saldo = pre.getSaldoPendiente() != null ? pre.getSaldoPendiente()
                : (pre.getTotal() != null ? pre.getTotal() : 0.0);

        if (monto > saldo) {
            throw new RuntimeException("El monto supera el saldo pendiente (S/ " + saldo + ")");
        }

        double adelanto = pre.getAdelanto() != null ? pre.getAdelanto() : 0.0;
        pre.setAdelanto(adelanto + monto);
        pre.setSaldoPendiente(saldo - monto);

        if (pre.getSaldoPendiente() <= 0.0001) {
            pre.setSaldoPendiente(0.0);
            pre.setEstado(EstadoPresupuesto.PAGADO);
        }

        return presupuestoRepo.save(pre);
    }

    private double calcularSubtotal(CasoClinico caso) {
        List<PlanTratamiento> planes = planRepo.findByCasoClinico(caso);
        double subtotal = 0.0;
        for (PlanTratamiento plan : planes) {
            if (plan.getEstado() == EstadoPlan.CANCELADO) continue;
            for (PlanDetalle d : planDetalleRepo.findByPlanOrderByOrdenAsc(plan)) {
                if (d.getEstado() == EstadoDetalle.CANCELADO) continue;
                if (d.getPrecio() != null) subtotal += d.getPrecio();
            }
        }
        return subtotal;
    }
}