package com.example.demo.dto;

import com.example.demo.model.Presupuesto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta de POST /api/presupuestos/{id}/pagos.
 * Cuando el pago cubre el saldo completo, informa que registros se actualizaron
 * automaticamente para que el frontend muestre la alerta correspondiente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroPagoDTO {

    private Presupuesto presupuesto;

    private boolean pagoCompletado;

    private String mensaje;

    private Integer citasFinalizadas;

    private Integer tratamientosRealizados;

    private boolean casoFinalizado;

    private boolean pacienteAtendido;
}
