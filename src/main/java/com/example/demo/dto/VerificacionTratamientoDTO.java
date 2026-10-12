package com.example.demo.dto;

import com.example.demo.model.EstadoDetalle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificacionTratamientoDTO {

    private Integer presupuestoId;
    private Integer casoId;
    private Integer pacienteId;
    private String pacienteNombre;
    private String pacienteApellido;
    private String dni;
    private Integer edad;
    private String odontologo;

    private String motivoConsulta;
    private String casoEstado;

    private Double subtotal;
    private Double descuento;
    private Double total;
    private Double adelanto;
    private Double saldoPendiente;
    private Double montoPersonalizado;
    private String estadoPresupuesto;

    private Integer tratamientosTotal;
    private Integer tratamientosPendientes;
    private Integer tratamientosRealizados;
    private Integer tratamientosCancelados;

    private Integer citasPendientes;
    private boolean pacienteAtendido;
}
