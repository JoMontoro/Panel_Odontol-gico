package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Registro completo de un odontograma, usado tanto para crear/editar
 * (el odontologo envia dientes, tejidos blandos y "otros") como para
 * responder el historial de evaluaciones del paciente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OdontogramaDTO {

    private Integer id;
    private Integer pacienteId;
    private String tipoDentadura;      // ADULTO | NINO
    private String observaciones;      // campo "Otros"
    private LocalDateTime fechaRegistro;
    private String odontologoNombre;

    private List<Diente> dientes;
    private List<Tejido> tejidosBlandos;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Diente {
        private Integer numeroDiente;
        private List<String> condiciones;   // nombres de CondicionDental
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Tejido {
        private String zona;
        private String estado;              // nombre de EstadoTejidoBlando
        private String observacion;
    }
}
