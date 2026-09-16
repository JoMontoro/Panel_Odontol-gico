package com.example.demo.service;

import com.example.demo.model.Paciente;
import com.example.demo.model.HistorialClinico;
import com.example.demo.model.CasoClinico;
import com.example.demo.model.PlanTratamiento;
import com.example.demo.model.Presupuesto;
import com.example.demo.model.ControlEvolucion;
import com.example.demo.repository.PacienteRepository;
import com.example.demo.repository.HistorialRepository;
import com.example.demo.repository.CasoClinicoRepository;
import com.example.demo.repository.PlanDetalleRepository;
import com.example.demo.repository.ControlEvolucionRepository;
import com.example.demo.repository.PresupuestoRepository;
import com.example.demo.repository.PlanTratamientoRepository;

import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PacienteService {

    @Autowired
    private PacienteRepository pacienteRepo;

    @Autowired
    private HistorialRepository historialRepo;

    @Autowired
    private CasoClinicoRepository casoRepo;

    @Autowired
    private PlanDetalleRepository planDetalleRepo;

    @Autowired
    private ControlEvolucionRepository controlRepo;

    @Autowired
    private PresupuestoRepository presupuestoRepo;

    @Autowired
    private PlanTratamientoRepository planRepo;

    public Paciente save(Paciente p) {
        Paciente saved = pacienteRepo.save(p);
        HistorialClinico h = historialRepo.findByPaciente(saved).orElse(null);
        if (h == null) {
            h = new HistorialClinico();
            h.setPaciente(saved);
            historialRepo.save(h);
        }
        return saved;
    }

    public Page<Paciente> listar(int page, int size, Boolean estado) {
        if (estado == null) {
            return pacienteRepo.findAll(PageRequest.of(page, size));
        }
        return pacienteRepo.findByEstado(PageRequest.of(page, size), estado);
    }

    public Optional<Paciente> buscarPorId(Integer id) {
        return pacienteRepo.findById(id);
    }

    public Paciente actualizar(Integer id, Paciente p) {
        Paciente existente = pacienteRepo.findById(id).orElseThrow();
        existente.setDni(p.getDni());
        existente.setNombre(p.getNombre());
        existente.setApellido(p.getApellido());
        existente.setFechaNacimiento(p.getFechaNacimiento());
        existente.setTelefono(p.getTelefono());
        existente.setEmail(p.getEmail());
        existente.setDireccion(p.getDireccion());
        existente.setApoderado(p.getApoderado());
        return pacienteRepo.save(existente);
    }

    public void desactivar(Integer id) {
        Paciente p = pacienteRepo.findById(id).orElseThrow();
        p.setEstado(false);
        pacienteRepo.save(p);
    }

    public void reactivar(Integer id) {
        Paciente p = pacienteRepo.findById(id).orElseThrow();
        p.setEstado(true);
        pacienteRepo.save(p);
    }

    public Map<String, Object> obtenerDetalle(Integer id) {
        Paciente paciente = pacienteRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Map<String, Object> response = new HashMap<>();
        response.put("paciente", paciente);

        // Historia clínica
        HistorialClinico historial = historialRepo.findByPaciente(paciente).orElse(null);
        response.put("historial", historial);

        // Casos clínicos
        List<CasoClinico> casos = casoRepo.findByPacienteOrderByFechaInicioDesc(paciente);
        List<Map<String, Object>> casosDto = new ArrayList<>();

        for (CasoClinico c : casos) {
            Map<String, Object> casoMap = new HashMap<>();
            casoMap.put("id", c.getId());
            casoMap.put("motivoConsulta", c.getMotivoConsulta());
            casoMap.put("fechaInicio", c.getFechaInicio());
            casoMap.put("fechaFin", c.getFechaFin());
            casoMap.put("estado", c.getEstado());
            casoMap.put("odontologo", c.getOdontologoResponsable().getNombre() + " " + c.getOdontologoResponsable().getApellido());

            // Presupuesto activo
            Presupuesto presupuesto = presupuestoRepo.findFirstByCasoClinicoOrderByIdDesc(c).orElse(null);
            casoMap.put("presupuestoId", presupuesto != null ? presupuesto.getId() : null);
            casoMap.put("saldoPendiente", presupuesto != null ? presupuesto.getSaldoPendiente() : 0);
            casoMap.put("totalPresupuesto", presupuesto != null ? presupuesto.getTotal() : 0);
            casoMap.put("adelanto", presupuesto != null ? presupuesto.getAdelanto() : 0);
            casoMap.put("estadoPresupuesto", presupuesto != null ? presupuesto.getEstado() : null);

            // Planes
            List<PlanTratamiento> planes = planRepo.findByCasoClinico(c);
            List<Map<String, Object>> planesDto = new ArrayList<>();
            for (PlanTratamiento p : planes) {
                Map<String, Object> planMap = new HashMap<>();
                planMap.put("id", p.getId());
                planMap.put("nombre", p.getNombre());
                planMap.put("estado", p.getEstado());
                planMap.put("sesionesEstimadas", p.getSesionesEstimadas());
                planMap.put("costoEstimado", p.getCostoEstimado());
                planesDto.add(planMap);
            }
            casoMap.put("planes", planesDto);

            casosDto.add(casoMap);
        }
        response.put("casosClinicos", casosDto);

        // Presupuesto del caso clínico activo (para el detalle de pago)
        Presupuesto presupuestoActivo = null;
        if (!casos.isEmpty()) {
            CasoClinico activo = casos.stream()
                    .filter(c -> c.getEstado() != com.example.demo.model.EstadoCaso.CANCELADO
                            && c.getEstado() != com.example.demo.model.EstadoCaso.FINALIZADO)
                    .findFirst()
                    .orElse(casos.get(0));
            presupuestoActivo = presupuestoRepo.findFirstByCasoClinicoOrderByIdDesc(activo).orElse(null);
        }
        response.put("presupuesto", presupuestoActivo);

        return response;
    }
}
