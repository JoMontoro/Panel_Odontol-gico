package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.model.Cita;
import com.example.demo.model.Paciente;
import com.example.demo.model.EstadoCita;
import com.example.demo.model.CasoClinico;
import com.example.demo.model.EstadoCaso;
import com.example.demo.repository.CitaRepository;
import com.example.demo.repository.PacienteRepository;
import com.example.demo.repository.CasoClinicoRepository;
import com.example.demo.repository.PagoRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    @Autowired
    private CitaRepository citaRepo;

    @Autowired
    private PacienteRepository pacienteRepo;

    @Autowired
    private CasoClinicoRepository casoRepo;

    @Autowired
    private PagoRepository pagoRepo;

    @Autowired
    private SesionActual sesion;

    /**
     * Las cifras del panel corresponden a la cartera del odontologo conectado.
     * El ADMIN (supervisor) ve los totales de ambos.
     */
    public DashboardDTO obtenerDashboard() {
        DashboardDTO dto = new DashboardDTO();
        LocalDate hoy = LocalDate.now();
        Integer odoId = sesion.odontologoId();
        boolean admin = odoId == SesionActual.SIN_FILTRO;

        dto.setCitasCompletadas(admin
                ? citaRepo.countByEstado(EstadoCita.FINALIZADA)
                : citaRepo.countByEstadoAndOdontologo_Id(EstadoCita.FINALIZADA, odoId));
        dto.setCitasPendientes(admin
                ? citaRepo.countByEstado(EstadoCita.PROGRAMADA)
                : citaRepo.countByEstadoAndOdontologo_Id(EstadoCita.PROGRAMADA, odoId));
        dto.setPacientesNuevos(admin ? pacienteRepo.count() : pacienteRepo.countByOdontologo_Id(odoId));
        dto.setCasosActivos(admin
                ? casoRepo.countByEstado(EstadoCaso.EN_PROCESO)
                : casoRepo.countByEstadoAndOdontologoResponsable_Id(EstadoCaso.EN_PROCESO, odoId));
        // Facturacion real del mes: suma de los abonos (en soles) registrados
        // desde el primer dia del mes hasta hoy, para la cartera del odontologo.
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate inicioMesSiguiente = inicioMes.plusMonths(1);
        Double facturadoMes = pagoRepo.sumarPorCarteraYRango(odoId, inicioMes, inicioMesSiguiente);
        dto.setFacturacionMensual(facturadoMes != null ? facturadoMes : 0.0);

        List<Cita> citasHoy = admin
                ? citaRepo.findByFechaOrderByHoraInicioAsc(hoy)
                : citaRepo.findByOdontologo_IdAndFechaOrderByHoraInicioAsc(odoId, hoy);
        List<CitaHoyDTO> citasHoyDTO = new ArrayList<>();

        for (Cita c : citasHoy) {
            CitaHoyDTO item = new CitaHoyDTO();
            item.setIdPaciente(c.getPaciente().getId());
            item.setHora(c.getHoraInicio().toString());
            item.setPaciente(c.getPaciente().getNombre() + " " + c.getPaciente().getApellido());
            item.setTratamiento(c.getCasoClinico().getMotivoConsulta());
            item.setEstado(c.getEstado().name());
            citasHoyDTO.add(item);
        }
        dto.setCitasHoy(citasHoyDTO);

        List<Paciente> pacientes = admin
                ? pacienteRepo.findByEstadoTrue()
                : pacienteRepo.findByEstadoAndOdontologo_IdOrderByIdDesc(true, odoId);
        List<PacienteRecienteDTO> recientes = new ArrayList<>();

        for (Paciente p : pacientes) {
            PacienteRecienteDTO item = new PacienteRecienteDTO();
            item.setId(p.getId());
            item.setNombre(p.getNombre() + " " + p.getApellido());
            item.setUltimaVisita(calcularUltimaVisita(p, hoy));
            recientes.add(item);
        }
        dto.setPacientesRecientes(recientes);

        return dto;
    }

    private String calcularUltimaVisita(Paciente paciente, LocalDate hoy) {
        return citaRepo.findFirstByPacienteOrderByFechaDesc(paciente)
                .map(cita -> {
                    LocalDate fechaVisita = cita.getFecha();
                    long dias = ChronoUnit.DAYS.between(fechaVisita, hoy);
                    if (dias == 0) return "Hoy";
                    if (dias == 1) return "Ayer";
                    if (dias < 7) return "Hace " + dias + " d\u00edas";
                    if (dias < 30) return "Hace " + (dias / 7) + " semanas";
                    return "Hace " + (dias / 30) + " meses";
                })
                .orElse("Sin visitas");
    }
}
