package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Cada abono que el paciente entrega queda registrado aqui con su fecha. El
 * acumulado del presupuesto (adelanto) dice cuanto lleva pagado; esta tabla,
 * ademas, dice cuando lo pago, para poder calcular la facturacion de un mes.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Double monto;

    private LocalDate fecha;

    @ManyToOne
    @JoinColumn(name = "id_presupuesto")
    private Presupuesto presupuesto;

    @PrePersist
    public void prePersist() {
        if (fecha == null) fecha = LocalDate.now();
    }
}
