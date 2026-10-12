package com.example.demo.model;

/**
 * Estado de atencion del paciente frente a sus tratamientos.
 * NULL o EN_ATENCION = paciente con tratamientos pendientes de pago.
 * ATENDIDO = todos sus tratamientos fueron pagados / el caso fue finalizado.
 * Es solo una etiqueta informativa: el paciente SIEMPRE permanece en la lista
 * de pacientes y solo se filtra si el usuario elige ver unicamente pendientes.
 */
public enum EstadoAtencionPaciente {
    EN_ATENCION,
    ATENDIDO
}
