package com.example.demo.service;


import com.example.demo.dto.*;
import com.example.demo.model.Cita;
import com.example.demo.model.Paciente;
import com.example.demo.repository.CitaRepository;
import com.example.demo.repository.PacienteRepository;

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

    public DashboardDTO obtenerDashboard() {

        DashboardDTO dto = new DashboardDTO();

        // KPIs
        dto.setCitasCompletadas(citaRepo.count());
        dto.setPacientesNuevos(pacienteRepo.count());
        dto.setFacturacionMensual(14500.0);

        // CITAS HOY
        List<CitaHoyDTO> citasHoy = new ArrayList<>();

        List<Cita> citas = citaRepo.findAll();

        for(Cita c : citas){

            CitaHoyDTO item = new CitaHoyDTO();

            item.setHora(c.getHora().toString());
            item.setPaciente(
                    c.getPaciente().getNombre()
                            + " "
                            + c.getPaciente().getApellido()
            );

            item.setTratamiento(c.getMotivo());
            item.setEstado(c.getEstado());

            citasHoy.add(item);

        }

        dto.setCitasHoy(citasHoy);

        // PACIENTES RECIENTES
        List<PacienteRecienteDTO> recientes =
                new ArrayList<>();

        List<Paciente> pacientes =
                pacienteRepo.findAll();

        for(Paciente p : pacientes){

            PacienteRecienteDTO item =
                    new PacienteRecienteDTO();

            item.setNombre(
                    p.getNombre() + " " + p.getApellido()
            );

            item.setUltimaVisita("Hace 1 semana");

            recientes.add(item);

        }

        dto.setPacientesRecientes(recientes);

        return dto;
    }
}
