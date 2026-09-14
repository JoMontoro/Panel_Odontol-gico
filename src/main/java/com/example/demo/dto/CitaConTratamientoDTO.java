package com.example.demo.dto;

import com.example.demo.model.EstadoCita;
import com.example.demo.model.EstadoDetalle;
import com.example.demo.model.TipoCita;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitaConTratamientoDTO {
    private Integer id;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private TipoCita tipo;
    private EstadoCita estadoCita;
    private String odontologoNombre;
    private String odontologoApellido;
    private String tratamientoNombre;
    private String categoria;
    private Integer diente;
    private String cara;
    private EstadoDetalle estadoDetalle;
    private Double precioEstimado;
    private String observaciones;
}