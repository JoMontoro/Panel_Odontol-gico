package com.example.demo.controller;

import com.example.demo.dto.ConsentimientoGuardarRequest;
import com.example.demo.dto.ConsentimientoInformadoResponse;
import com.example.demo.model.ConsentimientoInformado;
import com.example.demo.model.HistorialClinico;
import com.example.demo.service.ConsentimientoInformadoService;
import com.example.demo.repository.HistorialRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/consentimiento")
@CrossOrigin(origins = "http://localhost:4200")
public class ConsentimientoInformadoController {

    @Autowired
    private ConsentimientoInformadoService servicio;

    @Autowired
    private HistorialRepository historialRepo;

    /**
     * Guarda / acepta un consentimiento informado con firma del paciente y genera el PDF.
     * Cuerpo: ConsentimientoGuardarRequest (JSON).
     */
    @PostMapping
    public ResponseEntity<ConsentimientoInformado> guardar(
            @RequestBody ConsentimientoGuardarRequest req,
            HttpServletRequest httpRequest) {
        ConsentimientoInformado c = servicio.guardar(req, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(c);
    }

    /** Listado resumido de consentimientos de la cartera del odontologo conectado. */
    @GetMapping
    public List<ConsentimientoInformadoResponse> listar() {
        return servicio.listarResumido();
    }

    /** Detalle completo de un consentimiento (incluye firmas en base64 y contenido). */
    @GetMapping("/{id}")
    public ConsentimientoInformado obtener(@PathVariable Integer id) {
        return servicio.obtenerDetalle(id);
    }

    /** Consentimiento vigente vinculado al historial de un paciente. */
    @GetMapping("/paciente/{idPaciente}")
    public ConsentimientoInformado obtenerPorPaciente(@PathVariable Integer idPaciente) {
        HistorialClinico historial = historialRepo.findByPaciente_Id(idPaciente);
        if (historial == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "El paciente no tiene historial clínico.");
        }
        return servicio.obtenerPorHistorial(historial)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No hay consentimiento informado registrado para este paciente."));
    }

    /** Descarga el PDF generado del consentimiento. */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<Resource> descargarPdf(@PathVariable Integer id) {
        return servicio.descargarPdf(id);
    }
}
