package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Valoración de una zona de tejido blando (encías, paladar, lengua, ...)
 * dentro de un odontograma: estado mas una observación opcional.
 */
@Entity
@Table(name = "odontograma_tejido_blando")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OdontogramaTejidoBlando {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_odontograma", nullable = false)
    private Odontograma odontograma;

    /** Nombre de la zona: "Encías", "Paladar", "Lengua", ... */
    private String zona;

    @Enumerated(EnumType.STRING)
    private EstadoTejidoBlando estado;

    private String observacion;
}
