
package com.example.demo.service;

import com.example.demo.model.Paciente;
import com.example.demo.repository.PacienteRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;

@Service
public class PacienteService {
    @Autowired
    private PacienteRepository repo;
    
    // 🔹 CREAR
    public Paciente save(Paciente p){
        return repo.save(p);
    }

    // 🔹 LISTAR
    public Page<Paciente> listarPaginado(
            int page,
            int size
    ) {
        return repo.findAll(
                PageRequest.of(page, size)
        );
    }

    // 🔹 BUSCAR POR ID
    public Optional<Paciente> buscarPorId(Integer id){
        return repo.findById(id);
    }

    // 🔹 ACTUALIZAR
    public Paciente actualizar(Integer id, Paciente p){
        Paciente existente = repo.findById(id).orElseThrow();

        existente.setNombre(p.getNombre());
        existente.setApellido(p.getApellido());
        existente.setEdad(p.getEdad());
        existente.setApoderado(p.getApoderado());
        existente.setDireccion(p.getDireccion());
        existente.setDni(p.getDni());
        existente.setTelefono(p.getTelefono());
        existente.setEmail(p.getEmail());

        return repo.save(existente);
    }

    // 🔹 ELIMINAR
    public void eliminar(Integer id){
        repo.deleteById(id);
    }
    // 🔥 DETALLE PACIENTE
    public Map<String, Object> obtenerDetalle(Integer id) {

        Paciente paciente = repo
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Paciente no encontrado"
                        )
                );

        Map<String, Object> response = new HashMap<>();

        // =========================
        // PACIENTE
        // =========================
        response.put("paciente", paciente);

        // =========================
        // HISTORIAL
        // =========================
        Map<String, Object> historial =
                new HashMap<>();

        historial.put(
                "motivo_consulta",
                "Dolor dental"
        );

        historial.put(
                "antecedentes",
                "Diabetes"
        );

        historial.put(
                "tejidos_blandos",
                "Normal"
        );

        response.put(
                "historial",
                historial
        );

        // =========================
        // DIAGNOSTICO
        // =========================
        Map<String, Object> diagnostico =
                new HashMap<>();

        diagnostico.put(
                "descripcion",
                "Caries dental moderada"
        );

        response.put(
                "diagnostico",
                diagnostico
        );

        // =========================
        // PRESUPUESTO
        // =========================
        Map<String, Object> presupuesto =
                new HashMap<>();

        presupuesto.put("total", 1500);
        presupuesto.put("adelanto", 500);
        presupuesto.put("restante", 1000);

        response.put(
                "presupuesto",
                presupuesto
        );

        // =========================
        // TRATAMIENTOS
        // =========================
        List<Map<String, Object>>
                tratamientos = List.of(

                new HashMap<String, Object>() {{
                    put("tratamiento", "Endodoncia");
                    put("costo", 800);
                    put("sesiones", 3);
                }},

                new HashMap<String, Object>() {{
                    put("tratamiento", "Limpieza Dental");
                    put("costo", 200);
                    put("sesiones", 1);
                }}
        );

        response.put(
                "tratamientos",
                tratamientos
        );

        // =========================
        // EVOLUCIONES
        // =========================
        List<Map<String, Object>>
                evoluciones = List.of(

                new HashMap<String, Object>() {{
                    put("fecha", "2026-05-25");
                    put("procedimiento",
                            "Evaluación inicial");
                    put("acuenta", 300);
                    put("observaciones",
                            "Paciente estable");
                }}
        );

        response.put(
                "evoluciones",
                evoluciones
        );

        return response;
    }

}
