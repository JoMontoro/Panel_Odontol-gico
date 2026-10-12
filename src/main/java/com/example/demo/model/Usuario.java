package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String username;

    /** Nunca se expone en las respuestas JSON ( Paciente ya referencia a Usuario ). */
    @JsonIgnore
    private String password;
    private String nombre;
    private String apellido;
    private String correo;
    private String especialidad;
    private String foto;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    private Boolean estado;

    @PrePersist
    public void prePersist() {
        if (estado == null) estado = true;
    }
}
