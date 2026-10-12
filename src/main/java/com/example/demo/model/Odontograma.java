package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Odontograma clinico de una consulta: una fila por evaluacion del paciente.
 * Guarda la dentición usada (adulto o niño), el estado de cada diente
 * (malestares), la valoración de los tejidos blandos y el campo "Otros" con la
 * evaluación escrita del odontologo. Todo persiste en base de datos.
 *
 * Usa tablas propias (odontograma_evaluacion / _diente / _tejido_blando) para
 * no pisar la tabla legada "odontograma" de esquemas antiguos.
 */
@Entity
@Table(name = "odontograma_evaluacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Odontograma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    /** ADULTO (permanentes 18-48) o NINO (temporales 51-85). */
    @Enumerated(EnumType.STRING)
    private TipoDentadura tipoDentadura;

    /** Campo "Otros": evaluación libre del odontologo. */
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    private LocalDateTime fechaRegistro;

    /** Odontologo que registro la evaluación. */
    @ManyToOne
    @JoinColumn(name = "id_odontologo")
    private Usuario odontologo;

    @OneToMany(mappedBy = "odontograma", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OdontogramaDiente> dientes = new ArrayList<>();

    @OneToMany(mappedBy = "odontograma", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OdontogramaTejidoBlando> tejidosBlandos = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
    }
}
