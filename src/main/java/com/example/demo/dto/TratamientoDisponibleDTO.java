package com.example.demo.dto;

import com.example.demo.model.EstadoDetalle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TratamientoDisponibleDTO {
    private Integer planDetalleId;
    private Integer planTratamientoId;
    private String planNombre;
    private String tratamientoNombre;
    private String categoria;
    private Integer orden;
    private Integer diente;
    private String cara;
    private Double precioEstimado;
    private EstadoDetalle estadoDetalle;
    private Integer sesionesEstimadas;
    private Integer sesionesRealizadas;
}