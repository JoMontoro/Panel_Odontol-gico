package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanTratamiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String tratamiento;

    private Double costo;

    private Integer sesiones;

    @ManyToOne
    @JoinColumn(name = "id_historial")
    private HistorialClinico historial;
}
