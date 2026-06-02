package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class DashboardDTO {
    private Long citasCompletadas;
    private Long citasPendientes;
    private Long pacientesNuevos;
    private Double facturacionMensual;

    private List<CitaHoyDTO> citasHoy;
    private List<PacienteRecienteDTO> pacientesRecientes;
}
