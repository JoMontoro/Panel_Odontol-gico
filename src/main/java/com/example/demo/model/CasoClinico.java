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
public class CasoClinico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "id_odontologo_responsable", nullable = false)
    private Usuario odontologoResponsable;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String motivoConsulta;

    /**
     * Diagnostico clinico del caso. Es la base para armar automaticamente el plan
     * de tratamiento: el odontologo lo escribe y el sistema propone los
     * tratamientos del catalogo que corresponden, que luego puede editar.
     */
    private String diagnostico;

    @Enumerated(EnumType.STRING)
    private EstadoCaso estado;

    @PrePersist
    public void prePersist() {
        if (estado == null) estado = EstadoCaso.EN_PROCESO;
        if (fechaInicio == null) fechaInicio = LocalDate.now();
    }
}
