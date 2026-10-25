package com.example.demo.service;

import com.example.demo.model.Tratamiento;
import com.example.demo.model.Usuario;
import com.example.demo.repository.CitaRepository;
import com.example.demo.repository.PlanDetalleRepository;
import com.example.demo.repository.TratamientoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
     * Cada odontologo trabaja con su propio catalogo de tratamientos y no lo
     * comparte con el colega: solo el ADMIN supervisor consulta el de ambos.
     */
    public List<Tratamiento> listarActivos() {
        return repo.activosDe(sesion.odontologoId());
    }

    public List<Tratamiento> listarTodos(boolean incluirInactivos) {
        return repo.todosDe(sesion.odontologoId(), incluirInactivos);
    }

    public List<Tratamiento> listarPorCategoria(String categoria) {
        return repo.porCategoriaDe(sesion.odontologoId(), categoria);
    }

    public List<String> listarCategorias() {
        return repo.categoriasDe(sesion.odontologoId());
    }

    /** Solo se ve el tratamiento propio: el de otro odontologo no existe para esta sesion. */
    public Optional<Tratamiento> buscarPorId(Integer id) {
        Tratamiento t = repo.findById(id).orElse(null);
        if (t == null || !esVisible(t)) return Optional.empty();
        return Optional.of(t);
    }

    @Transactional
    public Tratamiento crear(Tratamiento datos) {
        Usuario actual = sesion.exigirEscritura();
        validar(datos, actual.getId(), null);
        datos.setId(null);
        datos.setOdontologo(actual);
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
        Usuario actual = sesion.exigirEscritura();
        Tratamiento existente = exigirPropio(id);

        validar(datos, actual.getId(), id);

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
        Tratamiento existente = exigirPropio(id);
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
        Tratamiento existente = exigirPropio(id);

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
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar de la base de datos: \"" + existente.getNombre() + "\" esta en uso por "
                            + detalle + ". Desactive el tratamiento en su lugar para conservarlo como historico.");
        }

        try {
            repo.delete(existente);
            repo.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se pudo eliminar el tratamiento: esta referenciado por otros registros.");
        }
    }

    /** El ADMIN supervisa (ver), pero no escribe en el catalogo de nadie. */
    private boolean esVisible(Tratamiento t) {
        Usuario actual = sesion.usuario();
        if (sesion.esAdmin(actual)) return true;
        return t.getOdontologo() != null && t.getOdontologo().getId().equals(actual.getId());
    }

    /** Carga el tratamiento y confirma que pertenece al catalogo del odontologo conectado. */
    private Tratamiento exigirPropio(Integer id) {
        Usuario actual = sesion.usuario();
        Tratamiento existente = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tratamiento no encontrado"));
        if (existente.getOdontologo() == null
                || !existente.getOdontologo().getId().equals(actual.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Ese tratamiento pertenece al catalogo de otro odontologo.");
        }
        return existente;
    }

    private void validar(Tratamiento datos, Integer odontoId, Integer idIgnorar) {
        if (datos.getNombre() == null || datos.getNombre().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del tratamiento es obligatorio");
        }
        if (datos.getPrecioReferencia() != null && datos.getPrecioReferencia() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El precio de referencia no puede ser negativo");
        }
        if (datos.getDuracionMinutos() != null && datos.getDuracionMinutos() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La duracion debe ser mayor a 0 minutos");
        }

        String codigo = normalizar(datos.getCodigo());
        if (codigo != null && !codigo.isEmpty()) {
            // El codigo es unico dentro del catalogo propio, no en el de los dos odontologos.
            boolean existe = idIgnorar == null
                    ? repo.existsByCodigoIgnoreCaseAndOdontologo_Id(codigo, odontoId)
                    : repo.existsByCodigoIgnoreCaseAndOdontologo_IdAndIdNot(codigo, odontoId, idIgnorar);
            if (existe) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Ya tienes un tratamiento con el codigo " + codigo);
            }
        }
    }

    private static String normalizar(String codigo) {
        if (codigo == null) return null;
        String limpio = codigo.trim().toUpperCase(Locale.ROOT);
        return limpio.isEmpty() ? null : limpio;
    }
}
