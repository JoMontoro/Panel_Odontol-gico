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

    public DashboardDTO obtenerDashboard() {
        DashboardDTO dto = new DashboardDTO();
        LocalDate hoy = LocalDate.now();

        dto.setCitasCompletadas(citaRepo.countByEstado(EstadoCita.FINALIZADA));
        dto.setCitasPendientes(citaRepo.countByEstado(EstadoCita.PROGRAMADA));
        dto.setPacientesNuevos(pacienteRepo.count());
        dto.setCasosActivos(casoRepo.countByEstado(EstadoCaso.EN_PROCESO));
        dto.setFacturacionMensual(14500.0);

        List<Cita> citasHoy = citaRepo.findByFechaOrderByHoraInicioAsc(hoy);
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

        List<Paciente> pacientes = pacienteRepo.findByEstadoTrue();
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
