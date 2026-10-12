package com.example.demo.service;

import com.example.demo.dto.VerificacionDetalleDTO;
import com.example.demo.dto.VerificacionTratamientoDTO;
import com.example.demo.model.CasoClinico;
import com.example.demo.model.Cita;
import com.example.demo.model.EstadoAtencionPaciente;
import com.example.demo.model.EstadoCita;
import com.example.demo.model.EstadoDetalle;
import com.example.demo.model.EstadoPlan;
import com.example.demo.model.EstadoPresupuesto;
import com.example.demo.model.Paciente;
import com.example.demo.model.PlanDetalle;
import com.example.demo.model.PlanTratamiento;
import com.example.demo.model.Presupuesto;
import com.example.demo.model.Usuario;
import com.example.demo.repository.CitaRepository;
import com.example.demo.repository.PlanDetalleRepository;
import com.example.demo.repository.PlanTratamientoRepository;
import com.example.demo.repository.PresupuestoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Alimenta el apartado de verificacion de tratamientos: revision de que
 * tratamientos estan cargados en la base de datos, cuanto suma cada plan, cuanto
 * pago el paciente y que registros se cierran al completar el pago.
 */
@Service
public class VerificacionTratamientoService {

    private static final List<EstadoCita> ESTADOS_CITA_ABIERTOS = Arrays.asList(
            EstadoCita.PROGRAMADA, EstadoCita.CONFIRMADA, EstadoCita.EN_ATENCION);

    @Autowired
    private PresupuestoRepository presupuestoRepo;

    @Autowired
    private PlanTratamientoRepository planRepo;

    @Autowired
    private PlanDetalleRepository planDetalleRepo;

    @Autowired
    private CitaRepository citaRepo;

    @Autowired
    private SesionActual sesion;

    public List<VerificacionTratamientoDTO> listar(String q, String estado) {
        String termino = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        String filtroEstado = estado == null ? "" : estado.trim().toUpperCase(Locale.ROOT);

        List<VerificacionTratamientoDTO> resultado = new ArrayList<>();

        // Solo los presupuestos de la cartera del odontologo conectado.
        for (Presupuesto presupuesto : presupuestoRepo.listarDeCartera(sesion.odontologoId())) {
            VerificacionTratamientoDTO fila = construirFila(presupuesto);
            if (fila == null) continue;
            if (!filtroEstado.isEmpty() && !filtroEstado.equals(fila.getEstadoPresupuesto())) continue;
            if (!termino.isEmpty() && !coincide(termino, fila)) continue;
            resultado.add(fila);
        }

        return resultado;
    }

    public Map<String, Object> resumen() {
        List<VerificacionTratamientoDTO> todas = listar(null, null);

        double porCobrar = 0.0;
        double cobrado = 0.0;
        int pendientes = 0;
        int pagados = 0;
        int atendidos = 0;
        int conTratamientos = 0;

        for (VerificacionTratamientoDTO fila : todas) {
            porCobrar += fila.getSaldoPendiente() != null ? fila.getSaldoPendiente() : 0.0;
            cobrado += fila.getAdelanto() != null ? fila.getAdelanto() : 0.0;
            if (fila.getTratamientosTotal() != null && fila.getTratamientosTotal() > 0) conTratamientos++;
            if (fila.isPacienteAtendido()) atendidos++;
            if (EstadoPresupuesto.PAGADO.name().equals(fila.getEstadoPresupuesto())) pagados++;
            else if (fila.getSaldoPendiente() != null && fila.getSaldoPendiente() > 0.0001) pendientes++;
        }

        Map<String, Object> mapa = new HashMap<>();
        mapa.put("totalPresupuestos", todas.size());
        mapa.put("conTratamientos", conTratamientos);
        mapa.put("pendientesPago", pendientes);
        mapa.put("pagados", pagados);
        mapa.put("pacientesAtendidos", atendidos);
        mapa.put("porCobrar", round2(porCobrar));
        mapa.put("cobrado", round2(cobrado));
        return mapa;
    }

    public VerificacionDetalleDTO detalle(Integer presupuestoId) {
        Presupuesto presupuesto = presupuestoRepo.findById(presupuestoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Presupuesto no encontrado"));

        if (presupuesto.getCasoClinico() == null
                || !sesion.esDeMiCartera(presupuesto.getCasoClinico().getPaciente())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Ese presupuesto pertenece a la cartera de otro odontologo.");
        }

        VerificacionTratamientoDTO resumen = construirFila(presupuesto);

        List<Map<String, Object>> planes = new ArrayList<>();
        for (PlanTratamiento plan : planRepo.findByCasoClinico_IdOrderByIdAsc(presupuesto.getCasoClinico().getId())) {
            Map<String, Object> planMap = new HashMap<>();
            planMap.put("id", plan.getId());
            planMap.put("nombre", plan.getNombre());
            planMap.put("estado", plan.getEstado());
            planMap.put("sesionesEstimadas", plan.getSesionesEstimadas());
            planMap.put("costoEstimado", plan.getCostoEstimado());

            List<Map<String, Object>> detalles = new ArrayList<>();
            for (PlanDetalle d : planDetalleRepo.findByPlanOrderByOrdenAsc(plan)) {
                Map<String, Object> dMap = new HashMap<>();
                dMap.put("id", d.getId());
                dMap.put("orden", d.getOrden());
                dMap.put("procedimiento", d.getProcedimiento());
                dMap.put("precio", d.getPrecio());
                dMap.put("estado", d.getEstado());
                dMap.put("diente", d.getDiente());
                dMap.put("cara", d.getCara());
                dMap.put("tratamientoId", d.getTratamiento() != null ? d.getTratamiento().getId() : null);
                dMap.put("categoria", d.getTratamiento() != null ? d.getTratamiento().getCategoria() : null);
                dMap.put("precioReferencia", d.getTratamiento() != null
                        ? d.getTratamiento().getPrecioReferencia() : null);
                detalles.add(dMap);
            }
            planMap.put("detalles", detalles);
            planes.add(planMap);
        }

        List<Map<String, Object>> citas = new ArrayList<>();
        for (Cita cita : citaRepo.findByCasoClinico_IdOrderByFechaAscHoraInicioAsc(
                presupuesto.getCasoClinico().getId())) {
            Map<String, Object> citaMap = new HashMap<>();
            citaMap.put("id", cita.getId());
            citaMap.put("fecha", cita.getFecha());
            citaMap.put("horaInicio", cita.getHoraInicio() != null ? cita.getHoraInicio().toString().substring(0, 5) : null);
            citaMap.put("estado", cita.getEstado());
            citaMap.put("tipo", cita.getTipo());
            citaMap.put("tratamiento", cita.getTratamiento() != null ? cita.getTratamiento().getNombre() : null);
            citas.add(citaMap);
        }

        return new VerificacionDetalleDTO(resumen, presupuesto, planes, citas);
    }

    private VerificacionTratamientoDTO construirFila(Presupuesto presupuesto) {
        CasoClinico caso = presupuesto.getCasoClinico();
        if (caso == null) return null;

        Paciente paciente = caso.getPaciente();

        int total = 0;
        int pendientes = 0;
        int realizados = 0;
        int cancelados = 0;

        for (PlanTratamiento plan : planRepo.findByCasoClinico_IdOrderByIdAsc(caso.getId())) {
            if (plan.getEstado() == EstadoPlan.CANCELADO) continue;
            for (PlanDetalle d : planDetalleRepo.findByPlanOrderByOrdenAsc(plan)) {
                total++;
                if (d.getEstado() == EstadoDetalle.REALIZADO) realizados++;
                else if (d.getEstado() == EstadoDetalle.CANCELADO) cancelados++;
                else pendientes++;
            }
        }

        int citasPendientes = (int) citaRepo.countByCasoClinico_IdAndEstadoIn(caso.getId(), ESTADOS_CITA_ABIERTOS);

        VerificacionTratamientoDTO dto = new VerificacionTratamientoDTO();
        dto.setPresupuestoId(presupuesto.getId());
        dto.setCasoId(caso.getId());
        dto.setPacienteId(paciente != null ? paciente.getId() : null);
        dto.setPacienteNombre(paciente != null ? paciente.getNombre() : null);
        dto.setPacienteApellido(paciente != null ? paciente.getApellido() : null);
        dto.setDni(paciente != null ? paciente.getDni() : null);
        dto.setEdad(calcularEdad(paciente));
        dto.setOdontologo(nombreOdontologo(caso.getOdontologoResponsable()));
        dto.setMotivoConsulta(caso.getMotivoConsulta());
        dto.setCasoEstado(caso.getEstado() != null ? caso.getEstado().name() : null);
        dto.setSubtotal(presupuesto.getSubtotal());
        dto.setDescuento(presupuesto.getDescuento());
        dto.setTotal(presupuesto.getTotal());
        dto.setAdelanto(presupuesto.getAdelanto());
        dto.setSaldoPendiente(presupuesto.getSaldoPendiente());
        dto.setMontoPersonalizado(presupuesto.getMontoPersonalizado());
        dto.setEstadoPresupuesto(presupuesto.getEstado() != null ? presupuesto.getEstado().name() : null);
        dto.setTratamientosTotal(total);
        dto.setTratamientosPendientes(pendientes);
        dto.setTratamientosRealizados(realizados);
        dto.setTratamientosCancelados(cancelados);
        dto.setCitasPendientes(citasPendientes);
        dto.setPacienteAtendido(paciente != null
                && paciente.getEstadoAtencion() == EstadoAtencionPaciente.ATENDIDO);

        return dto;
    }

    private boolean coincide(String termino, VerificacionTratamientoDTO fila) {
        return contiene(termino, fila.getPacienteNombre())
                || contiene(termino, fila.getPacienteApellido())
                || contiene(termino, fila.getDni())
                || contiene(termino, fila.getOdontologo())
                || contiene(termino, fila.getMotivoConsulta());
    }

    private boolean contiene(String termino, String valor) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(termino);
    }

    private Integer calcularEdad(Paciente paciente) {
        if (paciente == null || paciente.getFechaNacimiento() == null) return null;
        return Period.between(paciente.getFechaNacimiento(), LocalDate.now()).getYears();
    }

    private String nombreOdontologo(Usuario usuario) {
        if (usuario == null) return null;
        return join(usuario.getNombre(), usuario.getApellido());
    }

    private static String join(String nombre, String apellido) {
        return (nombre == null ? "" : nombre.trim()) + " " + (apellido == null ? "" : apellido.trim());
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
