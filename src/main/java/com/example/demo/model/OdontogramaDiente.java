package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Estado de un diente dentro del odontograma. El numero usa notación FDI
 * (11-48 permanentes, 51-85 temporales) y las condiciones son los malestares
 * que el odontologo marcó para ese diente.
 */
@Entity
@Table(name = "odontograma_diente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OdontogramaDiente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_odontograma", nullable = false)
    private Odontograma odontograma;

    private Integer numeroDiente;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "odontograma_diente_condiciones",
            joinColumns = @JoinColumn(name = "id_odontograma_diente"))
    @Column(name = "condicion")
    @Enumerated(EnumType.STRING)
    private Set<CondicionDental> condiciones = new HashSet<>();
}
