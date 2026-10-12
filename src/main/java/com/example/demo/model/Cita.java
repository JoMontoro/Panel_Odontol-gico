package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_caso_clinico", nullable = false)
    private CasoClinico casoClinico;

    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "id_odontologo", nullable = false)
    private Usuario odontologo;

    @ManyToOne
    @JoinColumn(name = "id_plan_detalle")
    private PlanDetalle planDetalle;

    @ManyToOne
    @JoinColumn(name = "id_tratamiento")
    private Tratamiento tratamiento;

    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    private TipoCita tipo;

    @Enumerated(EnumType.STRING)
    private EstadoCita estado;

    /**
     * Se activa cuando el paciente cancelo el total de su presupuesto de
     * tratamientos. La cita pasa a FINALIZADA y deja de mostrarse en el listado
     * de citas hasta que se active el filtro "Mostrar cerradas".
     */
    private Boolean cerradaPorPago;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @PrePersist
    public void prePersist() {
        if (estado == null) estado = com.example.demo.model.EstadoCita.PROGRAMADA;
        if (cerradaPorPago == null) cerradaPorPago = false;
    }
}
