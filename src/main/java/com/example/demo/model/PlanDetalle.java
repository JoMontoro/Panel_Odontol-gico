package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String procedimiento;
    private Integer orden;
    private Double precio;

    @Enumerated(EnumType.STRING)
    private EstadoDetalle estado;

    @ManyToOne
    @JoinColumn(name = "id_plan")
    private PlanTratamiento plan;

    @PrePersist
    public void prePersist() {
        if (estado == null) estado = EstadoDetalle.PENDIENTE;
    }
}
