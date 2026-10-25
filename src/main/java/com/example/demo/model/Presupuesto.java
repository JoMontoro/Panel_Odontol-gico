package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Presupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Double subtotal;
    private Double descuento;
    private Double total;
    private Double adelanto;
    private Double saldoPendiente;

    /**
     * Monto final a cobrar, editable por el odontologo. Cuando tiene valor,
     * sobrescribe el total calculado (subtotal - descuento). Si es null,
     * el total se recalcula a partir de los precios de los tratamientos.
     */
    private Double montoPersonalizado;

    @Enumerated(EnumType.STRING)
    private EstadoPresupuesto estado;

    @ManyToOne
    @JoinColumn(name = "id_caso_clinico")
    private CasoClinico casoClinico;

    @PrePersist
    public void prePersist() {
        if (estado == null) estado = EstadoPresupuesto.PENDIENTE;
    }
}
