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

    @PrePersist
    public void prePersist() {
        if (activo == null) activo = true;
    }
}