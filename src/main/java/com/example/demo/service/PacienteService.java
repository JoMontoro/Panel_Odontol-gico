package com.example.demo.service;

import com.example.demo.model.Paciente;
import com.example.demo.model.EstadoAtencionPaciente;
import com.example.demo.model.HistorialClinico;
import com.example.demo.model.CasoClinico;
import com.example.demo.model.PlanTratamiento;
import com.example.demo.model.Presupuesto;
import com.example.demo.model.ControlEvolucion;
import com.example.demo.model.Usuario;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    @Autowired
    private SesionActual sesion;

    /**
     * Alta de paciente: el dueno es siempre el odontologo conectado, sin importar
     * lo que envíe el formulario. Asi el paciente nunca se cuelga en el apartado
     * de otro profesional.
     */
    public Paciente save(Paciente p) {
        Usuario actual = sesion.exigirEscritura();
        p.setOdontologo(actual);

        Paciente saved = pacienteRepo.save(p);
        HistorialClinico h = historialRepo.findByPaciente(saved).orElse(null);
        if (h == null) {
            h = new HistorialClinico();
            h.setPaciente(saved);
            historialRepo.save(h);
        }
        return saved;
    }

    /**
     * Listado de pacientes. El estado ATENDIDO (presupuesto pagado / caso finalizado)
     * solo es una etiqueta: nunca saca al paciente del listado salvo que se pida
     * explicitamente mostrarAtendidos = false.
     */
    public Page<Paciente> listar(int page, int size, Boolean estado, boolean mostrarAtendidos) {
        Integer odoId = sesion.odontologoId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        if (mostrarAtendidos) {
            return pacienteRepo.listarPorEstado(estado, odoId, pageable);
        }
        return pacienteRepo.listarNoAtendidos(estado, odoId, EstadoAtencionPaciente.ATENDIDO, pageable);
    }

    public List<Paciente> listarAtendidos() {
        return pacienteRepo.listarAtendidos(
                sesion.odontologoId(),
                EstadoAtencionPaciente.ATENDIDO,
                PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }

    /** Pacientes propios, para los desplegables de la agenda. */
    public List<Paciente> listarCartera() {
        Integer odoId = sesion.odontologoId();
        if (odoId == SesionActual.SIN_FILTRO) {
            return pacienteRepo.findByEstadoTrue();
        }
        return pacienteRepo.findByOdontologo_IdOrderByIdDesc(odoId);
    }

    /** ADMIN puede ver cualquier paciente; un odontologo solo los suyos. */
    public Optional<Paciente> buscarPorId(Integer id) {
        Paciente p = pacienteRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));
        if (sesion.esAdmin(sesion.usuario())) {
            return Optional.of(p);
        }
        Integer dueno = p.getOdontologo() != null ? p.getOdontologo().getId() : null;
        if (dueno == null || !dueno.equals(sesion.usuario().getId())) {
            return Optional.empty();
        }
        return Optional.of(p);
    }

    public Paciente obtener(Integer id) {
        return buscarPorId(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));
    }

    public Paciente actualizar(Integer id, Paciente p) {
        Paciente existente = obtenerParaEscribir(id);
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

    /**
     * Oculta al paciente de la lista activa. Es un soft-delete: la ficha clinica,
     * los casos, el presupuesto y el historial se conservan intactos, y el
     * paciente vuelve a verse con el filtro "Mostrar ocultos".
     */
    public void desactivar(Integer id) {
        Paciente p = obtenerParaEscribir(id);
        p.setEstado(false);
        pacienteRepo.save(p);
    }

    public void reactivar(Integer id) {
        Paciente p = obtenerParaEscribir(id);
        p.setEstado(true);
        p.setEstadoAtencion(EstadoAtencionPaciente.EN_ATENCION);
        pacienteRepo.save(p);
    }

    /**
     * Devuelve al paciente al listado activo: lo desmarca como ATENDIDO sin
     * tocar el estado de sus tratamientos ni su historial clinico.
     */
    public void marcarEnAtencion(Integer id) {
        Paciente p = obtenerParaEscribir(id);
        p.setEstadoAtencion(EstadoAtencionPaciente.EN_ATENCION);
        pacienteRepo.save(p);
    }

    /** Carga el paciente y confirma que el odontologo conectado es su dueno. */
    public Paciente obtenerParaEscribir(Integer id) {
        Paciente p = pacienteRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));
        sesion.exigirDueño(p.getOdontologo() != null ? p.getOdontologo().getId() : null, "paciente");
        return p;
    }

    public Map<String, Object> obtenerDetalle(Integer id) {
        Paciente paciente = obtener(id);

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

                // Tratamientos del plan, con su precio editable
                List<Map<String, Object>> detallesDto = new ArrayList<>();
                for (com.example.demo.model.PlanDetalle d : planDetalleRepo.findByPlanOrderByOrdenAsc(p)) {
                    Map<String, Object> detMap = new HashMap<>();
                    detMap.put("id", d.getId());
                    detMap.put("orden", d.getOrden());
                    detMap.put("procedimiento", d.getProcedimiento());
                    detMap.put("precio", d.getPrecio());
                    detMap.put("estado", d.getEstado());
                    detMap.put("diente", d.getDiente());
                    detMap.put("cara", d.getCara());
                    detMap.put("tratamientoId", d.getTratamiento() != null ? d.getTratamiento().getId() : null);
                    detMap.put("precioReferencia", d.getTratamiento() != null
                            ? d.getTratamiento().getPrecioReferencia() : null);
                    detallesDto.add(detMap);
                }
                planMap.put("detalles", detallesDto);
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
