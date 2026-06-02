
package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Pattern;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paciente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_paciente;
    
    private String nombre;
    private String apellido;
    private Integer edad;
    private String apoderado;
    private String direccion;
    private Integer dni;

    @Pattern(
            regexp = "^9\\d{8}$",
            message = "El teléfono debe iniciar con 9 y tener 9 dígitos"
    )
    private String telefono;

    private String email;
    
}
