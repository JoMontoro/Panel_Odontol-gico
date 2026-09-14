package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanTratamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    private Integer sesionesEstimadas;
    private Double costoEstimado;

    @Enumerated(EnumType.STRING)
    private EstadoPlan estado;

    @ManyToOne
    @JoinColumn(name = "id_caso_clinico")
    private CasoClinico casoClinico;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanDetalle> planDetalles;

    @PrePersist
    public void prePersist() {
        if (estado == null) estado = EstadoPlan.PENDIENTE;
    }
}
