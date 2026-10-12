package com.example.demo.controller;

import com.example.demo.model.Paciente;
import com.example.demo.service.PacienteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pacientes")
@CrossOrigin(origins = "http://localhost:4200")
public class PacienteController {

    @Autowired
    private PacienteService service;

    @PostMapping
    public Paciente save(@RequestBody Paciente p) {
        return service.save(p);
    }

    /**
     * mostrarAtendidos viene en true por defecto: finalizar un caso o pagar el total
     * del presupuesto marca al paciente como ATENDIDO, pero nunca debe desaparecer
     * del listado. Solo se excluye si el usuario filtra explicitamente por pendientes.
     */
    @GetMapping
    public Page<Paciente> listar(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) Boolean estado,
                                  @RequestParam(defaultValue = "true") boolean mostrarAtendidos) {
        return service.listar(page, size, estado, mostrarAtendidos);
    }

    @GetMapping("/{id}")
    public Paciente obtener(@PathVariable Integer id) {
        return service.buscarPorId(id).orElseThrow();
    }

    @GetMapping("/{id}/detalle")
    public ResponseEntity<Map<String, Object>> obtenerDetalle(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtenerDetalle(id));
    }

    @PutMapping("/{id}")
    public Paciente actualizar(@PathVariable Integer id, @Validated @RequestBody Paciente p) {
        return service.actualizar(id, p);
    }

    @PutMapping("/{id}/reactivar")
    public void reactivar(@PathVariable Integer id) {
        service.reactivar(id);
    }

    /**
     * Oculta al paciente de la lista activa sin borrarlo: conserva su ficha,
     * casos, presupuesto e historial. Vuelve a verse con el filtro de ocultos.
     */
    @PutMapping("/{id}/desactivar")
    public void desactivar(@PathVariable Integer id) {
        service.desactivar(id);
    }

    /**
     * Retrocompatibilidad: el DELETE tambien es un soft-delete (ocultar), nunca
     * borra fisicamente al paciente.
     */
    @DeleteMapping("legacy/{id}")
    public void eliminarLegacy(@PathVariable Integer id) {
        service.desactivar(id);
    }

    /**
     * Devuelve al paciente al listado activo tras haber completado el pago de sus
     * tratamientos, sin alterar su historial clinico.
     */
    @PutMapping("/{id}/en-atencion")
    public void marcarEnAtencion(@PathVariable Integer id) {
        service.marcarEnAtencion(id);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        service.desactivar(id);
    }
}
