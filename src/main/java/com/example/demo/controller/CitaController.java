package com.example.demo.controller;

import com.example.demo.dto.CitaConTratamientoDTO;
import com.example.demo.dto.TratamientoDisponibleDTO;
import com.example.demo.model.Cita;
import com.example.demo.model.EstadoCita;
import com.example.demo.model.PlanDetalle;
import com.example.demo.service.CitaService;
import com.example.demo.service.PlanDetalleService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/citas")
@CrossOrigin(origins = "http://localhost:4200")
public class CitaController {

    @Autowired
    private CitaService service;

    @Autowired
    private PlanDetalleService planDetalleService;

    @PostMapping
    public Cita save(@RequestBody Cita c) {
        return service.save(c);
    }

    @GetMapping
    public Page<Cita> listar(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) String q,
                             @RequestParam(required = false) EstadoCita estado) {
        return service.listarPaginado(page, size, q, estado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtener(@PathVariable Integer id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Cita actualizar(@PathVariable Integer id, @RequestBody Cita c) {
        return service.actualizar(id, c);
    }

    @PutMapping("/{id}/cancelar")
    public void cancelar(@PathVariable Integer id) {
        service.cancelar(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            service.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", e.getMessage()));
        }
    }

    @GetMapping("/fecha/{fecha}")
    public List<Cita> listarPorFecha(@PathVariable String fecha) {
        return service.listarPorFecha(java.time.LocalDate.parse(fecha));
    }

    @GetMapping("/odontologo/{idOdontologo}/fecha/{fecha}")
    public List<Cita> listarPorOdontologoYFecha(
            @PathVariable Integer idOdontologo,
            @PathVariable String fecha) {
        return service.listarPorOdontologoYFecha(idOdontologo, java.time.LocalDate.parse(fecha));
    }

    @GetMapping("/paciente/{pacienteId}/tratamientos-disponibles")
    public List<TratamientoDisponibleDTO> obtenerTratamientosDisponibles(@PathVariable Integer pacienteId) {
        List<PlanDetalle> detalles = planDetalleService.listarPendientesPorPaciente(pacienteId);
        return detalles.stream().map(d -> new TratamientoDisponibleDTO(
                d.getId(),
                d.getPlan().getId(),
                d.getPlan().getNombre(),
                d.getTratamiento() != null ? d.getTratamiento().getNombre() : d.getProcedimiento(),
                d.getTratamiento() != null ? d.getTratamiento().getCategoria() : "SIN_CATEGORIA",
                d.getOrden(),
                d.getDiente(),
                d.getCara(),
                d.getPrecio(),
                d.getEstado(),
                d.getPlan().getSesionesEstimadas(),
                (int) d.getPlan().getPlanDetalles().stream().filter(pd -> pd.getEstado() == com.example.demo.model.EstadoDetalle.REALIZADO).count()
        )).collect(Collectors.toList());
    }

    @GetMapping("/paciente/{pacienteId}/con-tratamiento")
    public List<CitaConTratamientoDTO> listarConTratamientoPorPaciente(@PathVariable Integer pacienteId) {
        List<Cita> citas = service.listarPorPaciente(pacienteId);
        return citas.stream().map(c -> {
            PlanDetalle pd = c.getPlanDetalle();
            return new CitaConTratamientoDTO(
                    c.getId(),
                    c.getFecha(),
                    c.getHoraInicio(),
                    c.getHoraFin(),
                    c.getTipo(),
                    c.getEstado(),
                    c.getOdontologo() != null ? c.getOdontologo().getNombre() : null,
                    c.getOdontologo() != null ? c.getOdontologo().getApellido() : null,
                    pd != null && pd.getTratamiento() != null ? pd.getTratamiento().getNombre() : 
                        (pd != null ? pd.getProcedimiento() : c.getCasoClinico().getMotivoConsulta()),
                    pd != null && pd.getTratamiento() != null ? pd.getTratamiento().getCategoria() : "SIN_CATEGORIA",
                    pd != null ? pd.getDiente() : null,
                    pd != null ? pd.getCara() : null,
                    pd != null ? pd.getEstado() : null,
                    pd != null ? pd.getPrecio() : null,
                    c.getObservaciones()
            );
        }).collect(Collectors.toList());
    }
}
