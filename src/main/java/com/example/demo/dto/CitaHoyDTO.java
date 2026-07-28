package com.example.demo.dto;

import lombok.Data;

@Data
public class CitaHoyDTO {
    private Integer idPaciente;
    private String hora;
    private String paciente;
    private String tratamiento;
    private String estado;
}
