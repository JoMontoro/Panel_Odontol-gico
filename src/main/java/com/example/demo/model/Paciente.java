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
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String dni;
    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String email;
    private String direccion;
    private String apoderado;
    private Boolean estado;

    /**
     * Se marca como ATENDIDO en automatico cuando el paciente cancela el total
     * de su presupuesto o se finaliza su caso. Es solo informativo: el paciente
     * sigue apareciendo en la lista, con su etiqueta de atendido.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_atencion")
    private EstadoAtencionPaciente estadoAtencion;

    /**
     * Odontologo dueno del paciente. Cada odontologo trabaja con su propia
     * cartera: un paciente registrado por uno no aparece en el listado del otro.
     * Solo el rol ADMIN (supervisor) puede ver la cartera completa.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_odontologo")
    private Usuario odontologo;

    @PrePersist
    public void prePersist() {
        if (estado == null) estado = true;
        if (estadoAtencion == null) estadoAtencion = EstadoAtencionPaciente.EN_ATENCION;
    }
}
