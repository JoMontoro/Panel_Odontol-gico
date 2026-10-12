package com.example.demo.service;

import com.example.demo.model.Rol;
import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Identifica al odontologo que esta operando y aplica el aislamiento de datos:
 * cada odontologo solo ve y modifica sus propios pacientes, citas, casos,
 * presupuestos y verificaciones. El rol ADMIN es un supervisor de solo lectura
 * y por eso ve el historico completo de ambos odontologos.
 */
@Service
public class SesionActual {

    /**
     * Valor centinela que se envia a las consultas cuando el usuario puede ver
     * todos los registros (ADMIN). Evita comparar contra un parametro null en HQL.
     */
    public static final Integer SIN_FILTRO = -1;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private com.example.demo.repository.PacienteRepository pacienteRepo;

    public Usuario usuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null
                || "anonymousUser".equals(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesion no iniciada");
        }
        return usuarioRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }

    public boolean esAdmin(Usuario u) {
        return u != null && u.getRol() == Rol.ADMIN;
    }

    /** Id que se usa como filtro: el propio odontologo, o SIN_FILTRO si es ADMIN. */
    public Integer odontologoId(Usuario u) {
        return esAdmin(u) ? SIN_FILTRO : u.getId();
    }

    public Integer odontologoId() {
        return odontologoId(usuario());
    }

    /** El ADMIN supervisa, pero no registra ni modifica informacion clinica. */
    public Usuario exigirEscritura() {
        Usuario u = usuario();
        if (esAdmin(u)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El rol ADMIN es solo de lectura: no puede registrar ni modificar informacion clinica.");
        }
        return u;
    }

    /**
     * Impide que un odontologo toque un registro que pertenece a otro.
     * ADMIN puede leer cualquier registro, pero no escribirlo.
     */
    public Usuario exigirDueño(Integer duenoId, String recurso) {
        Usuario actual = usuario();
        if (esAdmin(actual)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El rol ADMIN es solo de lectura: no puede modificar " + recurso + ".");
        }
        if (duenoId == null || !duenoId.equals(actual.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Este " + recurso + " pertenece a otro odontologo.");
        }
        return actual;
    }

    private Integer duenoDe(com.example.demo.model.Paciente paciente) {
        return paciente != null && paciente.getOdontologo() != null ? paciente.getOdontologo().getId() : null;
    }

    /**
     * Alta/edicion sobre un paciente de la cartera. El ADMIN queda excluido por ser
     * de solo lectura.
     */
    public Usuario exigirPacienteDeCartera(com.example.demo.model.Paciente paciente) {
        Usuario actual = exigirEscritura();
        Integer dueno = duenoDe(paciente);
        if (dueno == null || !dueno.equals(actual.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Ese paciente pertenece a la cartera de otro odontologo.");
        }
        return actual;
    }

    /** Igual que el anterior, resolviendo el caso clinico a su paciente. */
    public Usuario exigirCasoDeCartera(com.example.demo.model.CasoClinico caso) {
        return exigirPacienteDeCartera(caso != null ? caso.getPaciente() : null);
    }

    /** Lectura: el ADMIN puede ver cualquier cartera, el odontologo solo la suya. */
    public boolean esDeMiCartera(com.example.demo.model.Paciente paciente) {
        Usuario actual = usuario();
        if (esAdmin(actual)) return true;
        Integer dueno = duenoDe(paciente);
        return dueno != null && dueno.equals(actual.getId());
    }

    /** Carga un paciente y confirma que el usuario puede leerlo. */
    public com.example.demo.model.Paciente exigirPacienteVisible(Integer pacienteId) {
        com.example.demo.model.Paciente p = pacienteRepo.findById(pacienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));
        if (!esDeMiCartera(p)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Ese paciente pertenece a la cartera de otro odontologo.");
        }
        return p;
    }
}
