package com.example.demo.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    /**
     * Los detalles se consultan por su propio endpoint. Sin esta anotacion el
     * plan se serializa con todos sus detalles y cada detalle vuelve a traer el
     * plan completo, generando un JSON giganticamente anidado.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanDetalle> planDetalles;

    @PrePersist
    public void prePersist() {
        if (estado == null) estado = EstadoPlan.PENDIENTE;
    }
}
