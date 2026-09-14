package com.example.demo.service;

import com.example.demo.model.PlanDetalle;
import com.example.demo.model.PlanTratamiento;
import com.example.demo.model.Tratamiento;
import com.example.demo.model.EstadoDetalle;
import com.example.demo.repository.PlanDetalleRepository;
import com.example.demo.repository.PlanTratamientoRepository;
import com.example.demo.repository.TratamientoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PlanDetalleService {

    @Autowired
    private PlanDetalleRepository repo;

    @Autowired
    private PlanTratamientoRepository planRepo;

    @Autowired
    private TratamientoRepository tratamientoRepo;

    public List<PlanDetalle> listarPorPlan(Integer planId) {
        PlanTratamiento plan = planRepo.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan de tratamiento no encontrado"));
        return repo.findByPlanOrderByOrdenAsc(plan);
    }

    public List<PlanDetalle> listarPendientesPorPlan(Integer planId) {
        PlanTratamiento plan = planRepo.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan de tratamiento no encontrado"));
        return repo.findByPlanAndEstado(plan, EstadoDetalle.PENDIENTE);
    }

    public List<PlanDetalle> listarPendientesPorPaciente(Integer pacienteId) {
        return repo.findByPlan_CasoClinico_Paciente_IdAndEstado(pacienteId, EstadoDetalle.PENDIENTE);
    }

    public Optional<PlanDetalle> buscarPorId(Integer id) {
        return repo.findById(id);
    }

    @Transactional
    public PlanDetalle agregarDetalle(Integer planId, Integer tratamientoId, Integer orden, Integer diente, String cara) {
        PlanTratamiento plan = planRepo.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan de tratamiento no encontrado"));
        
        Tratamiento tratamiento = tratamientoRepo.findById(tratamientoId)
                .orElseThrow(() -> new RuntimeException("Tratamiento no encontrado"));

        PlanDetalle detalle = new PlanDetalle();
        detalle.setPlan(plan);
        detalle.setTratamiento(tratamiento);
        detalle.setProcedimiento(tratamiento.getNombre());
        detalle.setOrden(orden);
        detalle.setPrecio(tratamiento.getPrecioReferencia());
        detalle.setDiente(diente);
        detalle.setCara(cara);
        detalle.setEstado(EstadoDetalle.PENDIENTE);

        return repo.save(detalle);
    }

    @Transactional
    public PlanDetalle actualizarEstado(Integer detalleId, EstadoDetalle estado) {
        PlanDetalle detalle = repo.findById(detalleId)
                .orElseThrow(() -> new RuntimeException("Detalle de plan no encontrado"));
        detalle.setEstado(estado);
        return repo.save(detalle);
    }

    @Transactional
    public void eliminar(Integer detalleId) {
        repo.deleteById(detalleId);
    }
}