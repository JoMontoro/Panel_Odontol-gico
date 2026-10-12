package com.example.demo.controller;

import com.example.demo.dto.RegistroPagoDTO;
import com.example.demo.model.Presupuesto;
import com.example.demo.service.PresupuestoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/presupuestos")
@CrossOrigin(origins = "http://localhost:4200")
public class PresupuestoController {

    @Autowired
    private PresupuestoService service;

    @GetMapping("/caso/{casoId}")
    public ResponseEntity<?> obtenerPorCaso(@PathVariable Integer casoId) {
        return service.buscarPorCaso(casoId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/caso/{casoId}/todos")
    public List<Presupuesto> listarPorCaso(@PathVariable Integer casoId) {
        return service.listarPorCaso(casoId);
    }

    @PostMapping("/caso/{casoId}")
    public ResponseEntity<?> generar(@PathVariable Integer casoId,
                                     @RequestBody(required = false) Map<String, Object> body) {
        Double descuento = 0.0;
        if (body != null && body.get("descuento") != null) {
            descuento = Double.valueOf(body.get("descuento").toString());
        }
        try {
            return ResponseEntity.ok(service.generar(casoId, descuento));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/{presupuestoId}/aprobar")
    public ResponseEntity<?> aprobar(@PathVariable Integer presupuestoId) {
        try {
            return ResponseEntity.ok(service.aprobar(presupuestoId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    /**
     * Fija el monto total a cobrar al paciente. Si el monto deja el saldo en cero,
     * se ejecuta la misma cascada de cierre que un pago completo.
     */
    @PutMapping("/{presupuestoId}/monto")
    public ResponseEntity<?> actualizarMonto(@PathVariable Integer presupuestoId,
                                             @RequestBody Map<String, Object> body) {
        if (body == null || body.get("monto") == null) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Debe enviar el monto a cobrar"));
        }
        try {
            Double monto = Double.valueOf(body.get("monto").toString());
            RegistroPagoDTO resultado = service.actualizarMonto(presupuestoId, monto);
            return ResponseEntity.ok(resultado);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El monto no es un numero valido"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Aplica un descuento y el total a cobrar baja en la misma medida. */
    @PutMapping("/{presupuestoId}/descuento")
    public ResponseEntity<?> actualizarDescuento(@PathVariable Integer presupuestoId,
                                                @RequestBody Map<String, Object> body) {
        if (body == null || body.get("descuento") == null) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Debe enviar el descuento"));
        }
        try {
            Double descuento = Double.valueOf(body.get("descuento").toString());
            return ResponseEntity.ok(service.actualizarDescuento(presupuestoId, descuento));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El descuento no es un numero valido"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Vuelve al total calculado por los precios de los tratamientos. */
    @DeleteMapping("/{presupuestoId}/monto")
    public ResponseEntity<?> quitarMontoPersonalizado(@PathVariable Integer presupuestoId) {
        try {
            return ResponseEntity.ok(service.quitarMontoPersonalizado(presupuestoId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    /** Recalcula el subtotal del presupuesto a partir de los precios actuales. */
    @PostMapping("/caso/{casoId}/recalcular")
    public ResponseEntity<?> recalcular(@PathVariable Integer casoId) {
        try {
            return ResponseEntity.ok(service.recalcularCaso(casoId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PostMapping("/{presupuestoId}/pagos")
    public ResponseEntity<?> registrarPago(@PathVariable Integer presupuestoId,
                                           @RequestBody Map<String, Object> body) {
        Double monto = body != null && body.get("monto") != null
                ? Double.valueOf(body.get("monto").toString()) : 0.0;
        try {
            RegistroPagoDTO resultado = service.registrarPago(presupuestoId, monto);
            return ResponseEntity.ok(resultado);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El monto no es un numero valido"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }
}
