package com.example.demo.dto;

import com.example.demo.model.Presupuesto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificacionDetalleDTO {

    private VerificacionTratamientoDTO resumen;

    private Presupuesto presupuesto;

    private List<Map<String, Object>> planes;

    private List<Map<String, Object>> citas;
}
