package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Genera el plan de tratamiento del caso a partir de los tratamientos que
 * indico el odontologo. El precio de cada item sale del catalogo, salvo que se
 * envie uno propio para ese paciente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerarPlanDTO {

    private String nombre;
    private Integer sesionesEstimadas;
    private List<Item> tratamientos;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Integer tratamientoId;
        private Integer orden;
        private Integer diente;
        private String cara;
        private Double precio;
    }
}
