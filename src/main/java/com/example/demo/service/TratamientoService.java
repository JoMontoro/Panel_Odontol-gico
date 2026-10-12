package com.example.demo.service;

import com.example.demo.model.Tratamiento;
import com.example.demo.repository.CitaRepository;
import com.example.demo.repository.PlanDetalleRepository;
import com.example.demo.repository.TratamientoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class TratamientoService {

    @Autowired
    private TratamientoRepository repo;

    @Autowired
    private PlanDetalleRepository planDetalleRepo;

    @Autowired
    private CitaRepository citaRepo;

    @Autowired
    private SesionActual sesion;

    /**
     * El catalogo de tratamientos es el unico dato compartido entre odontologos.
     * Ambos pueden consultarlo, pero el ADMIN supervisor no lo modifica.
     */
    public List<Tratamiento> listarActivos() {
        return repo.findByActivoTrueOrderByNombreAsc();
    }

    public List<Tratamiento> listarTodos(boolean incluirInactivos) {
        return incluirInactivos
                ? repo.findAllByOrderByCategoriaAscNombreAsc()
                : repo.findByActivoTrueOrderByNombreAsc();
    }

    public List<Tratamiento> listarPorCategoria(String categoria) {
        return repo.findByCategoriaOrderByNombreAsc(categoria);
    }

    public List<String> listarCategorias() {
        return repo.findCategorias();
    }

    public Optional<Tratamiento> buscarPorId(Integer id) {
        return repo.findById(id);
    }

    @Transactional
    public Tratamiento crear(Tratamiento datos) {
        sesion.exigirEscritura();
        validar(datos, null);
        datos.setId(null);
        datos.setCodigo(normalizar(datos.getCodigo()));
        datos.setNombre(datos.getNombre().trim());
        if (datos.getCategoria() != null) {
            datos.setCategoria(datos.getCategoria().trim().toUpperCase(Locale.ROOT));
        }
        if (datos.getActivo() == null) datos.setActivo(true);
        return repo.save(datos);
    }

    @Transactional
    public Tratamiento actualizar(Integer id, Tratamiento datos) {
        sesion.exigirEscritura();
        Tratamiento existente = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Tratamiento no encontrado"));

        validar(datos, id);

        existente.setCodigo(normalizar(datos.getCodigo()));
        existente.setNombre(datos.getNombre().trim());
        existente.setDescripcion(datos.getDescripcion());
        existente.setDuracionMinutos(datos.getDuracionMinutos());
        existente.setPrecioReferencia(datos.getPrecioReferencia());
        if (datos.getCategoria() != null) {
            existente.setCategoria(datos.getCategoria().trim().toUpperCase(Locale.ROOT));
        } else {
            existente.setCategoria(null);
        }
        if (datos.getActivo() != null) {
            existente.setActivo(datos.getActivo());
        }
        return repo.save(existente);
    }

    /**
     * Baja logica: el tratamiento deja de ofrecerse al crear citas y planes,
     * pero se conserva en la base de datos para no romper el historial clinico.
     */
    @Transactional
    public Tratamiento cambiarEstado(Integer id, boolean activo) {
        sesion.exigirEscritura();
        Tratamiento existente = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Tratamiento no encontrado"));
        existente.setActivo(activo);
        return repo.save(existente);
    }

    /**
     * Eliminacion definitiva del registro en la base de datos. Solo se permite si
     * ningun plan ni cita lo referencia; en caso contrario se informa el detalle.
     */
    @Transactional
    public void eliminar(Integer id) {
        sesion.exigirEscritura();
        Tratamiento existente = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Tratamiento no encontrado"));

        boolean enPlanes = planDetalleRepo.existsByTratamiento_Id(id);
        boolean enCitas = citaRepo.existsByTratamiento_Id(id);

        if (enPlanes || enCitas) {
            StringBuilder detalle = new StringBuilder();
            if (enPlanes) {
                detalle.append("planes de tratamiento");
            }
            if (enCitas) {
                if (detalle.length() > 0) detalle.append(" y ");
                detalle.append("citas");
            }
            throw new RuntimeException(
                    "No se puede eliminar de la base de datos: \"" + existente.getNombre() + "\" esta en uso por "
                            + detalle + ". Desactive el tratamiento en su lugar para conservarlo como historico.");
        }

        try {
            repo.delete(existente);
            repo.flush();
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("No se pudo eliminar el tratamiento: esta referenciado por otros registros.");
        }
    }

    private void validar(Tratamiento datos, Integer idIgnorar) {
        if (datos.getNombre() == null || datos.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre del tratamiento es obligatorio");
        }
        if (datos.getPrecioReferencia() != null && datos.getPrecioReferencia() < 0) {
            throw new RuntimeException("El precio de referencia no puede ser negativo");
        }
        if (datos.getDuracionMinutos() != null && datos.getDuracionMinutos() <= 0) {
            throw new RuntimeException("La duracion debe ser mayor a 0 minutos");
        }

        String codigo = normalizar(datos.getCodigo());
        if (codigo != null && !codigo.isEmpty()) {
            boolean existe = idIgnorar == null
                    ? repo.existsByCodigoIgnoreCase(codigo)
                    : repo.existsByCodigoIgnoreCaseAndIdNot(codigo, idIgnorar);
            if (existe) {
                throw new RuntimeException("Ya existe un tratamiento con el codigo " + codigo);
            }
        }
    }

    private static String normalizar(String codigo) {
        if (codigo == null) return null;
        String limpio = codigo.trim().toUpperCase(Locale.ROOT);
        return limpio.isEmpty() ? null : limpio;
    }
}
