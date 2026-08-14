package com.example.demo.service;

import com.example.demo.model.Cita;
import com.example.demo.model.CasoClinico;
import com.example.demo.model.EstadoCita;
import com.example.demo.repository.CitaRepository;
import com.example.demo.repository.CasoClinicoRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class CitaService {

    @Autowired
    private CitaRepository repo;

    @Autowired
    private CasoClinicoRepository casoRepo;

    public Cita save(Cita cita) {
        CasoClinico caso = casoRepo.findById(cita.getCasoClinico().getId())
                .orElseThrow(() -> new RuntimeException("Caso clínico no encontrado"));
        cita.setCasoClinico(caso);
        cita.setPaciente(caso.getPaciente());
        return repo.save(cita);
    }

    public Page<Cita> listarPaginado(int page, int size, String q, EstadoCita estado) {
        String termino = (q == null ? "" : q.trim());
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("fecha"), Sort.Order.asc("horaInicio")));
        if (estado != null) {
            return repo.buscarPorEstadoYTermino(termino, estado, pageable);
        }
        return repo.buscarPorTermino(termino, pageable);
    }

    public Optional<Cita> buscarPorId(Integer id) {
        return repo.findById(id);
    }

    public Cita actualizar(Integer id, Cita data) {
        Cita cita = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        if (data.getCasoClinico() != null && data.getCasoClinico().getId() != null) {
            CasoClinico caso = casoRepo.findById(data.getCasoClinico().getId())
                    .orElseThrow(() -> new RuntimeException("Caso clínico no encontrado"));
            cita.setCasoClinico(caso);
            cita.setPaciente(caso.getPaciente());
        }
        if (data.getOdontologo() != null) {
            cita.setOdontologo(data.getOdontologo());
        }
        if (data.getFecha() != null) cita.setFecha(data.getFecha());
        if (data.getHoraInicio() != null) cita.setHoraInicio(data.getHoraInicio());
        if (data.getHoraFin() != null) cita.setHoraFin(data.getHoraFin());
        if (data.getTipo() != null) cita.setTipo(data.getTipo());
        if (data.getEstado() != null) cita.setEstado(data.getEstado());
        if (data.getObservaciones() != null) cita.setObservaciones(data.getObservaciones());
        return repo.save(cita);
    }

    public void cancelar(Integer id) {
        Cita cita = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        cita.setEstado(com.example.demo.model.EstadoCita.CANCELADA);
        repo.save(cita);
    }

    public void eliminar(Integer id) {
        Cita cita = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        try {
            repo.delete(cita);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new RuntimeException(
                    "No se puede eliminar: la cita tiene evoluciones u otros registros asociados. Cáncelala en su lugar.");
        }
    }

    public List<Cita> listarPorFecha(java.time.LocalDate fecha) {
        return repo.findByFechaOrderByHoraInicioAsc(fecha);
    }

    public List<Cita> listarPorOdontologoYFecha(Integer idOdontologo, java.time.LocalDate fecha) {
        com.example.demo.model.Usuario odontologo = new com.example.demo.model.Usuario();
        odontologo.setId(idOdontologo);
        return repo.findByOdontologoAndFechaOrderByHoraInicioAsc(odontologo, fecha);
    }
}
