package com.example.demo.service;

import com.example.demo.model.CasoClinico;
import com.example.demo.model.Paciente;
import com.example.demo.model.Usuario;
import com.example.demo.repository.CasoClinicoRepository;
import com.example.demo.repository.PacienteRepository;
import com.example.demo.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class CasoClinicoService {

    @Autowired
    private CasoClinicoRepository repo;

    @Autowired
    private PacienteRepository pacienteRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private SesionActual sesion;

    public List<CasoClinico> listarPorPaciente(Integer pacienteId) {
        Paciente paciente = pacienteRepo.findById(pacienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));
        exigirPacientePropio(paciente);
        return repo.findByPacienteOrderByFechaInicioDesc(paciente);
    }

    public Optional<CasoClinico> buscarPorId(Integer id) {
        CasoClinico caso = repo.findById(id).orElse(null);
        if (caso == null) return Optional.empty();
        Integer odoId = sesion.odontologoId();
        if (odoId == SesionActual.SIN_FILTRO) return Optional.of(caso);
        Integer dueno = caso.getPaciente() != null && caso.getPaciente().getOdontologo() != null
                ? caso.getPaciente().getOdontologo().getId() : null;
        if (dueno == null || !dueno.equals(odoId)) return Optional.empty();
        return Optional.of(caso);
    }

    /** El caso clinico se abre sobre un paciente propio y con oneself como responsable. */
    public CasoClinico crear(Integer pacienteId, String username, String motivoConsulta, String diagnostico) {
        Usuario actual = sesion.exigirEscritura();
        Paciente paciente = pacienteRepo.findById(pacienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));
        exigirPacientePropio(paciente);

        CasoClinico caso = new CasoClinico();
        caso.setPaciente(paciente);
        caso.setOdontologoResponsable(actual);
        caso.setMotivoConsulta(motivoConsulta);
        caso.setDiagnostico(diagnostico);
        return repo.save(caso);
    }

    public CasoClinico actualizar(Integer id, String motivoConsulta, String estado) {
        return actualizar(id, motivoConsulta, estado, null, false);
    }

    /** El diagnostico solo se toca si viene en el cuerpo de la peticion. */
    public CasoClinico actualizar(Integer id, String motivoConsulta, String estado,
                                   String diagnostico, boolean editarDiagnostico) {
        CasoClinico caso = obtenerParaEscribir(id);
        if (motivoConsulta != null && !motivoConsulta.isBlank())
            caso.setMotivoConsulta(motivoConsulta);
        if (editarDiagnostico)
            caso.setDiagnostico(diagnostico != null ? diagnostico.trim() : null);
        if (estado != null && !estado.isBlank())
            caso.setEstado(com.example.demo.model.EstadoCaso.valueOf(estado));
        return repo.save(caso);
    }

    public void cancelar(Integer id) {
        CasoClinico caso = obtenerParaEscribir(id);
        caso.setEstado(com.example.demo.model.EstadoCaso.CANCELADO);
        repo.save(caso);
    }

    private CasoClinico obtenerParaEscribir(Integer id) {
        CasoClinico caso = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caso clinico no encontrado"));
        Integer dueno = caso.getPaciente() != null && caso.getPaciente().getOdontologo() != null
                ? caso.getPaciente().getOdontologo().getId() : null;
        sesion.exigirDueño(dueno, "caso clinico");
        return caso;
    }

    private void exigirPacientePropio(Paciente paciente) {
        if (sesion.esAdmin(sesion.usuario())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El rol ADMIN es solo de lectura: no puede abrir casos clinicos.");
        }
        Integer dueno = paciente.getOdontologo() != null ? paciente.getOdontologo().getId() : null;
        if (dueno == null || !dueno.equals(sesion.usuario().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Ese paciente pertenece a la cartera de otro odontologo.");
        }
    }
}
