package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tratamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String codigo;
    private String nombre;
    private String descripcion;
    private Integer duracionMinutos;
    private Double precioReferencia;
    private String categoria;
    private Boolean activo;

    /**
     * Odontologo dueno del tratamiento: cada odontologo tiene su propio
     * catalogo y no lo comparte con el colega. Solo el rol ADMIN (supervisor
     * de solo lectura) consulta el catalogo de los dos. Los tratamientos que
     * venian precargados en la base quedan sin dueno y se retiran (o se
     * asignan a quien ya los uso) en el arranque de la aplicacion.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_odontologo")
    private Usuario odontologo;

    @PrePersist
    public void prePersist() {
        if (activo == null) activo = true;
    }
}