package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ControlEvolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_caso_clinico", nullable = false)
    private CasoClinico casoClinico;

    @ManyToOne
    @JoinColumn(name = "id_cita")
    private Cita cita;

    private LocalDate fecha;

    @Column(columnDefinition = "TEXT")
    private String procedimientoRealizado;

    private Double montoPagado;

    @Column(columnDefinition = "TEXT")
    private String observaciones;
}
