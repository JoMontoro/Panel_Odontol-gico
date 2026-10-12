package com.example.demo.service;

import com.example.demo.dto.OdontogramaDTO;
import com.example.demo.model.*;
import com.example.demo.repository.OdontogramaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Alta, consulta y edición de los odontogramas del paciente.
 * Aplica las mismas reglas de cartera que el resto del sistema: el odontologo
 * solo opera sobre sus propios pacientes y el rol ADMIN es de solo lectura.
 */
@Service
public class OdontogramaService {

    @Autowired
    private OdontogramaRepository repo;

    @Autowired
    private SesionActual sesion;

    // ============================ CONSULTA ============================

    @Transactional(readOnly = true)
    public List<OdontogramaDTO> listarPorPaciente(Integer pacienteId) {
        if (pacienteId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el identificador del paciente");
        }
        Paciente paciente = sesion.exigirPacienteVisible(pacienteId);
        return repo.findByPacienteOrderByFechaRegistroDesc(paciente).stream()
                .map(this::aDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public OdontogramaDTO obtener(Integer id) {
        Odontograma odontograma = buscar(id);
        sesion.exigirPacienteVisible(odontograma.getPaciente().getId());
        return aDTO(odontograma);
    }

    // ============================ ESCRITURA ============================

    @Transactional
    public OdontogramaDTO crear(OdontogramaDTO dto) {
        Usuario actual = sesion.exigirEscritura();
        if (dto == null || dto.getPacienteId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El odontograma debe pertenecer a un paciente");
        }
        Paciente paciente = sesion.exigirPacienteVisible(dto.getPacienteId());

        Odontograma odontograma = new Odontograma();
        odontograma.setPaciente(paciente);
        odontograma.setOdontologo(actual);
        aplicarContenido(odontograma, dto);
        return aDTO(repo.save(odontograma));
    }

    @Transactional
    public OdontogramaDTO actualizar(Integer id, OdontogramaDTO dto) {
        sesion.exigirEscritura();
        Odontograma odontograma = buscar(id);
        sesion.exigirPacienteDeCartera(odontograma.getPaciente());
        aplicarContenido(odontograma, dto);
        return aDTO(repo.save(odontograma));
    }

    @Transactional
    public void eliminar(Integer id) {
        sesion.exigirEscritura();
        Odontograma odontograma = buscar(id);
        sesion.exigirPacienteDeCartera(odontograma.getPaciente());
        repo.delete(odontograma);
    }

    // ============================ INTERNOS ============================

    private Odontograma buscar(Integer id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el identificador del odontograma");
        }
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Odontograma no encontrado"));
    }

    /**
     * Valida el payload y reemplaza el contenido del odontograma (dientes,
     * tejidos blandos y "otros") por el enviado. Los dientes sin malestares y
     * las zonas sin valoración no se persisten.
     */
    private void aplicarContenido(Odontograma odontograma, OdontogramaDTO dto) {
        TipoDentadura tipo = validarTipo(dto.getTipoDentadura());
        validarDientes(dto, tipo);
        validarTejidos(dto);

        odontograma.setTipoDentadura(tipo);
        odontograma.setObservaciones(
                dto.getObservaciones() == null || dto.getObservaciones().isBlank()
                        ? null : dto.getObservaciones().trim());

        odontograma.getDientes().clear();
        odontograma.getTejidosBlandos().clear();

        if (dto.getDientes() != null) {
            for (OdontogramaDTO.Diente d : dto.getDientes()) {
                Set<CondicionDental> condiciones = new HashSet<>();
                if (d.getCondiciones() != null) {
                    d.getCondiciones().forEach(c -> condiciones.add(CondicionDental.valueOf(c)));
                }
                if (condiciones.isEmpty()) continue;

                OdontogramaDiente od = new OdontogramaDiente();
                od.setOdontograma(odontograma);
                od.setNumeroDiente(d.getNumeroDiente());
                od.setCondiciones(condiciones);
                odontograma.getDientes().add(od);
            }
        }

        if (dto.getTejidosBlandos() != null) {
            for (OdontogramaDTO.Tejido t : dto.getTejidosBlandos()) {
                boolean sinEstado = t.getEstado() == null || t.getEstado().isBlank();
                boolean sinObs = t.getObservacion() == null || t.getObservacion().isBlank();
                if (sinEstado && sinObs) continue;

                OdontogramaTejidoBlando ot = new OdontogramaTejidoBlando();
                ot.setOdontograma(odontograma);
                ot.setZona(t.getZona().trim());
                ot.setEstado(sinEstado ? EstadoTejidoBlando.NO_EVALUADO : EstadoTejidoBlando.valueOf(t.getEstado()));
                ot.setObservacion(sinObs ? null : t.getObservacion().trim());
                odontograma.getTejidosBlandos().add(ot);
            }
        }
    }

    private TipoDentadura validarTipo(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Indique si la dentición es ADULTO o NIÑO");
        }
        try {
            return TipoDentadura.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Tipo de dentición inválido: " + valor);
        }
    }

    /** El numero de cada diente debe existir en la dentición seleccionada. */
    private void validarDientes(OdontogramaDTO dto, TipoDentadura tipo) {
        Set<Integer> permitidos = numerosPermitidos(tipo);
        Set<Integer> vistos = new HashSet<>();

        if (dto.getDientes() == null) return;
        for (OdontogramaDTO.Diente d : dto.getDientes()) {
            if (d.getNumeroDiente() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Diente sin número");
            }
            if (!permitidos.contains(d.getNumeroDiente())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El diente " + d.getNumeroDiente() + " no existe en la dentición "
                                + (tipo == TipoDentadura.ADULTO ? "adulta" : "infantil"));
            }
            if (!vistos.add(d.getNumeroDiente())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El diente " + d.getNumeroDiente() + " aparece más de una vez");
            }
            if (d.getCondiciones() != null) {
                for (String c : d.getCondiciones()) {
                    try {
                        CondicionDental.valueOf(c);
                    } catch (IllegalArgumentException | NullPointerException e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Malestar desconocido: " + c);
                    }
                }
            }
        }
    }

    private void validarTejidos(OdontogramaDTO dto) {
        if (dto.getTejidosBlandos() == null) return;
        for (OdontogramaDTO.Tejido t : dto.getTejidosBlandos()) {
            if (t == null || t.getZona() == null || t.getZona().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tejido blando sin zona");
            }
            if (t.getEstado() != null && !t.getEstado().isBlank()) {
                try {
                    EstadoTejidoBlando.valueOf(t.getEstado());
                } catch (IllegalArgumentException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Estado desconocido para la zona " + t.getZona() + ": " + t.getEstado());
                }
            }
        }
    }

    /** Numeración FDI válida por dentición. */
    private Set<Integer> numerosPermitidos(TipoDentadura tipo) {
        Set<Integer> numeros = new HashSet<>();
        if (tipo == TipoDentadura.ADULTO) {
            for (int cuadrante = 1; cuadrante <= 4; cuadrante++) {
                for (int pos = 1; pos <= 8; pos++) numeros.add(cuadrante * 10 + pos);
            }
        } else {
            for (int cuadrante = 5; cuadrante <= 8; cuadrante++) {
                for (int pos = 1; pos <= 5; pos++) numeros.add(cuadrante * 10 + pos);
            }
        }
        return numeros;
    }

    private OdontogramaDTO aDTO(Odontograma o) {
        OdontogramaDTO dto = new OdontogramaDTO();
        dto.setId(o.getId());
        dto.setPacienteId(o.getPaciente() != null ? o.getPaciente().getId() : null);
        dto.setTipoDentadura(o.getTipoDentadura() != null ? o.getTipoDentadura().name() : null);
        dto.setObservaciones(o.getObservaciones());
        dto.setFechaRegistro(o.getFechaRegistro());
        if (o.getOdontologo() != null) {
            String nombre = (o.getOdontologo().getNombre() != null ? o.getOdontologo().getNombre() : "")
                    + (o.getOdontologo().getApellido() != null ? " " + o.getOdontologo().getApellido() : "");
            dto.setOdontologoNombre(nombre.trim());
        }

        List<OdontogramaDTO.Diente> dientes = new ArrayList<>();
        for (OdontogramaDiente d : o.getDientes()) {
            List<String> condiciones = new ArrayList<>();
            d.getCondiciones().forEach(c -> condiciones.add(c.name()));
            condiciones.sort(Comparator.naturalOrder());
            dientes.add(new OdontogramaDTO.Diente(d.getNumeroDiente(), condiciones));
        }
        dientes.sort(Comparator.comparing(OdontogramaDTO.Diente::getNumeroDiente));
        dto.setDientes(dientes);

        List<OdontogramaDTO.Tejido> tejidos = new ArrayList<>();
        for (OdontogramaTejidoBlando t : o.getTejidosBlandos()) {
            tejidos.add(new OdontogramaDTO.Tejido(
                    t.getZona(),
                    t.getEstado() != null ? t.getEstado().name() : null,
                    t.getObservacion()));
        }
        dto.setTejidosBlandos(tejidos);

        return dto;
    }
}
