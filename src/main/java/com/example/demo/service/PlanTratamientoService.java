package com.example.demo.service;

import com.example.demo.dto.GenerarPlanDTO;
import com.example.demo.model.PlanTratamiento;
import com.example.demo.model.PlanDetalle;
import com.example.demo.model.Tratamiento;
import com.example.demo.model.CasoClinico;
import com.example.demo.model.EstadoDetalle;
import com.example.demo.model.Paciente;
import com.example.demo.model.EstadoPlan;
import com.example.demo.repository.PlanTratamientoRepository;
import com.example.demo.repository.PlanDetalleRepository;
import com.example.demo.repository.TratamientoRepository;
import com.example.demo.repository.CasoClinicoRepository;
import com.example.demo.repository.PacienteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PlanTratamientoService {

    private static final String PREFIJO_SUGERIDO = "Plan sugerido";
    private static final int MAX_SUGERENCIAS = 12;

    private static final Set<String> PALABRAS_VACIAS = new HashSet<>(Arrays.asList(
            "dental", "dentista", "tratamiento", "procedimiento", "paciente", "consulta",
            "inicial", "diagnostico", "simple", "comun"));

    /**
     * Como el odontologo describe el problema con sus palabras, se cruzan los
     * terminos clinicos frecuentes contra el catalogo. La clave es lo que puede
     * escribir el odontologo y el valor son las palabras del catalogo que lo
     * resuelven.
     */
    private static final Map<String, List<String>> PALABRAS_CLAVE = new HashMap<>();

    static {
        PALABRAS_CLAVE.put("caries", Arrays.asList("resina", "restauracion", "obturacion"));
        PALABRAS_CLAVE.put("cariado", Arrays.asList("resina", "restauracion"));
        PALABRAS_CLAVE.put("muela", Arrays.asList("endodoncia", "exodoncia", "corona"));
        PALABRAS_CLAVE.put("dolor", Arrays.asList("endodoncia", "exodoncia", "conducto"));
        PALABRAS_CLAVE.put("absceso", Arrays.asList("endodoncia", "exodoncia", "drenaje"));
        PALABRAS_CLAVE.put("infeccion", Arrays.asList("endodoncia", "exodoncia"));
        PALABRAS_CLAVE.put("sensacion", Arrays.asList("endodoncia"));
        PALABRAS_CLAVE.put("fractura", Arrays.asList("corona", "protesis", "endodoncia", "incrustacion"));
        PALABRAS_CLAVE.put("rotura", Arrays.asList("corona", "protesis", "puente"));
        PALABRAS_CLAVE.put("rota", Arrays.asList("corona", "protesis", "puente"));
        PALABRAS_CLAVE.put("juicio", Arrays.asList("cordal", "exodoncia", "cirugia"));
        PALABRAS_CLAVE.put("cordal", Arrays.asList("cordal", "exodoncia", "cirugia"));
        PALABRAS_CLAVE.put("tercero", Arrays.asList("cordal", "exodoncia"));
        PALABRAS_CLAVE.put("sangrado", Arrays.asList("periodon", "gingival", "profilaxis", "raspado"));
        PALABRAS_CLAVE.put("encias", Arrays.asList("periodon", "gingival", "profilaxis"));
        PALABRAS_CLAVE.put("sarro", Arrays.asList("profilaxis", "raspado", "destartraje"));
        PALABRAS_CLAVE.put("placa", Arrays.asList("profilaxis", "raspado", "destartraje"));
        PALABRAS_CLAVE.put("malolor", Arrays.asList("blanqueamiento", "carilla", "microabrasil"));
        PALABRAS_CLAVE.put("estetica", Arrays.asList("blanqueamiento", "carilla", "microabrasil"));
        PALABRAS_CLAVE.put("amarillos", Arrays.asList("blanqueamiento"));
        PALABRAS_CLAVE.put("implante", Arrays.asList("implante", "injerto"));
        PALABRAS_CLAVE.put("perdid", Arrays.asList("implante", "protesis", "puente", "puente"));
        PALABRAS_CLAVE.put("protesis", Arrays.asList("protesis", "puente"));
        PALABRAS_CLAVE.put("dentadura", Arrays.asList("protesis"));
        PALABRAS_CLAVE.put("torcido", Arrays.asList("ortodoncia", "brackets", "alineador"));
        PALABRAS_CLAVE.put("torcidos", Arrays.asList("ortodoncia", "brackets", "alineador"));
        PALABRAS_CLAVE.put("alineacion", Arrays.asList("ortodoncia", "brackets", "alineador"));
        PALABRAS_CLAVE.put("brackets", Arrays.asList("brackets", "ortodoncia"));
        PALABRAS_CLAVE.put("sellado", Arrays.asList("sellante", "fluor", "preventivo"));
        PALABRAS_CLAVE.put("fluor", Arrays.asList("fluor", "sellante", "preventivo"));
        PALABRAS_CLAVE.put("prevencion", Arrays.asList("sellante", "fluor", "preventivo"));
        PALABRAS_CLAVE.put("radiografia", Arrays.asList("radiografia", "panoramica", "periapical", "tomografia"));
        PALABRAS_CLAVE.put("radiograf", Arrays.asList("radiografia", "panoramica", "periapical", "tomografia"));
        PALABRAS_CLAVE.put("cbct", Arrays.asList("tomografia", "cbct"));
        PALABRAS_CLAVE.put("tomografia", Arrays.asList("tomografia", "cbct"));
    }

    private static class Sugerencia {
        final Tratamiento tratamiento;
        final int puntaje;

        Sugerencia(Tratamiento tratamiento, int puntaje) {
            this.tratamiento = tratamiento;
            this.puntaje = puntaje;
        }
    }

    @Autowired
    private PlanTratamientoRepository repo;

    @Autowired
    private PlanDetalleRepository detalleRepo;

    @Autowired
    private TratamientoRepository tratamientoRepo;

    @Autowired
    private PresupuestoService presupuestoService;

    @Autowired
    private CasoClinicoRepository casoRepo;

    @Autowired
    private PacienteRepository pacienteRepo;

    @Autowired
    private SesionActual sesion;

    private CasoClinico casoDeCartera(Integer casoId) {
        CasoClinico caso = casoRepo.findById(casoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caso clinico no encontrado"));
        if (!sesion.esDeMiCartera(caso.getPaciente())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Ese caso clinico pertenece a la cartera de otro odontologo.");
        }
        return caso;
    }

    /**
     * El plan solo puede cotizar tratamientos del catalogo propio: el catalogo de
     * cada odontologo es privado y no se comparte con el colega.
     */
    private void exigirTratamientoPropio(Tratamiento tratamiento) {
        if (sesion.esAdmin(sesion.usuario())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El rol ADMIN es solo de lectura: no puede armar planes de tratamiento.");
        }
        Integer odontoId = sesion.odontologoId();
        if (tratamiento.getOdontologo() == null
                || !tratamiento.getOdontologo().getId().equals(odontoId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El tratamiento \"" + tratamiento.getNombre() + "\" pertenece al catalogo de otro odontologo.");
        }
    }

    public List<PlanTratamiento> listarPorCaso(Integer casoId) {
        return repo.findByCasoClinico(casoDeCartera(casoId));
    }

    public List<PlanTratamiento> listarActivosPorPaciente(Integer pacienteId) {
        Paciente paciente = sesion.exigirPacienteVisible(pacienteId);
        return repo.findByCasoClinicoPacienteAndEstado(paciente, EstadoPlan.EN_PROCESO);
    }

    /**
     * Plan vigente del caso: sirve para Agregar tratamientos sobre el mismo plan.
     * Un plan ya liquidado (COMPLETADO) o cancelado no se reutiliza, de modo que
     * cuando el presupuesto llega a cero el caso queda con el plan vacio y el
     * siguiente genera uno nuevo desde cero.
     */
    public Optional<PlanTratamiento> buscarActivoPorCaso(Integer casoId) {
        return repo.findByCasoClinico(casoDeCartera(casoId)).stream()
                .filter(p -> p.getEstado() != EstadoPlan.CANCELADO && p.getEstado() != EstadoPlan.COMPLETADO)
                .findFirst();
    }

    @Transactional
    public PlanTratamiento crear(Integer casoId, String nombre, Integer sesionesEstimadas, Double costoEstimado) {
        CasoClinico caso = casoDeCartera(casoId);
        sesion.exigirEscritura();

        PlanTratamiento plan = new PlanTratamiento();
        plan.setCasoClinico(caso);
        plan.setNombre(nombre);
        plan.setSesionesEstimadas(sesionesEstimadas);
        plan.setCostoEstimado(costoEstimado);
        plan.setEstado(EstadoPlan.PENDIENTE);

        return repo.save(plan);
    }

    @Transactional
    public PlanTratamiento actualizarEstado(Integer planId, EstadoPlan estado) {
        PlanTratamiento plan = repo.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan de tratamiento no encontrado"));
        sesion.exigirCasoDeCartera(plan.getCasoClinico());

        plan.setEstado(estado);
        return repo.save(plan);
    }

    /**
     * Arma el plan del caso con los tratamientos que indico el odontologo, usando
     * el precio de referencia del catalogo como precio a cobrar. Si el caso ya
     * tiene un plan vigente lo reutiliza en lugar de duplicarlo, de modo que el
     * presupuesto siempre sale de los mismos tratamientos que ve el odontologo.
     * Si el plan anterior ya esta liquidado, se crea uno nuevo vacio.
     */
    @Transactional
    public PlanTratamiento generarDesdeTratamientos(Integer casoId, GenerarPlanDTO dto) {
        CasoClinico caso = casoDeCartera(casoId);
        sesion.exigirEscritura();

        if (dto == null || dto.getTratamientos() == null || dto.getTratamientos().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Seleccione al menos un tratamiento para el plan");
        }

        PlanTratamiento plan = buscarActivoPorCaso(casoId).orElse(null);
        if (plan == null) {
            String nombre = dto.getNombre() != null && !dto.getNombre().isBlank()
                    ? dto.getNombre().trim()
                    : "Plan de tratamiento";
            plan = repo.save(nuevoPlan(caso, nombre,
                    dto.getSesionesEstimadas() != null ? dto.getSesionesEstimadas() : 1));
        }

        int orden = detalleRepo.findByPlanOrderByOrdenAsc(plan).size();
        double total = 0.0;

        for (GenerarPlanDTO.Item item : dto.getTratamientos()) {
            if (item == null || item.getTratamientoId() == null) continue;

            Tratamiento tratamiento = tratamientoRepo.findById(item.getTratamientoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "El tratamiento seleccionado no existe en el catalogo"));

            exigirTratamientoPropio(tratamiento);

            if (!Boolean.TRUE.equals(tratamiento.getActivo())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El tratamiento \"" + tratamiento.getNombre() + "\" esta inactivo. "
                                + "Actívelo en el catálogo antes de agregarlo al plan.");
            }

            orden++;
            Double precio = item.getPrecio() != null ? item.getPrecio() : tratamiento.getPrecioReferencia();

            PlanDetalle detalle = new PlanDetalle();
            detalle.setPlan(plan);
            detalle.setTratamiento(tratamiento);
            detalle.setProcedimiento(tratamiento.getNombre());
            detalle.setOrden(item.getOrden() != null ? item.getOrden() : orden);
            detalle.setPrecio(precio);
            detalle.setDiente(item.getDiente());
            detalle.setCara(item.getCara());
            detalle.setEstado(EstadoDetalle.PENDIENTE);
            detalleRepo.save(detalle);

            if (precio != null) total += precio;
        }

        plan.setCostoEstimado(round2(total));
        repo.save(plan);

        // Si el caso ya tiene presupuesto, se recalcula con los tratamientos nuevos.
        presupuestoService.recalcularCaso(casoId);

        return plan;
    }

    /**
     * Arma el plan del caso automaticamente a partir del diagnostico y del motivo
     * de consulta, proponiendo los tratamientos del catalogo que corresponden.
     * El odontologo puede editarlo freely: cambiar precios, quitar o agregar
     * tratamientos, y el presupuesto se recalcula solo.
     *
     * El plan generado es una sugerencia, no una imposicion: si el caso ya tiene
     * un plan vigente se completa con las propuestas que falten, y con forzar se
     * reemplazan los tratamientos sugeridos que aun esten pendientes.
     */
    @Transactional
    public PlanTratamiento generarDesdeDiagnostico(Integer casoId, boolean forzar) {
        CasoClinico caso = casoDeCartera(casoId);
        sesion.exigirEscritura();

        String texto = normalizar(join(
                caso.getDiagnostico() != null ? caso.getDiagnostico() : "",
                caso.getMotivoConsulta() != null ? caso.getMotivoConsulta() : ""));

        if (texto.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Registre el diagnostico o el motivo de consulta del caso para poder sugerir el plan.");
        }

        List<PlanTratamiento> planes = repo.findByCasoClinico(caso);
        PlanTratamiento plan = planes.stream()
                .filter(p -> p.getEstado() != EstadoPlan.CANCELADO && p.getEstado() != EstadoPlan.COMPLETADO)
                .findFirst()
                .orElse(null);

        if (plan == null) {
            plan = repo.save(nuevoPlan(caso, PREFIJO_SUGERIDO, 1));
        }

        List<PlanDetalle> actuales = detalleRepo.findByPlanOrderByOrdenAsc(plan);

        if (forzar) {
            for (PlanDetalle d : actuales) {
                if (d.getEstado() == EstadoDetalle.PENDIENTE) {
                    detalleRepo.delete(d);
                }
            }
            detalleRepo.flush();
            actuales = detalleRepo.findByPlanOrderByOrdenAsc(plan);
        }

        List<PlanDetalle> restantes = actuales.stream()
                .filter(d -> d.getEstado() != EstadoDetalle.CANCELADO)
                .collect(Collectors.toList());

        List<Sugerencia> sugerencias = sugerirTratamientos(texto);
        if (sugerencias.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se encontro ningun tratamiento del catalogo que corresponda al diagnostico. "
                            + "Registre el diagnostico con mayor detalle o arme el plan manualmente.");
        }

        int orden = actuales.size();
        double total = 0.0;
        for (PlanDetalle d : restantes) {
            if (d.getPrecio() != null) total += d.getPrecio();
        }

        for (Sugerencia s : sugerencias) {
            boolean yaEsta = restantes.stream().anyMatch(d ->
                    d.getTratamiento() != null && d.getTratamiento().getId().equals(s.tratamiento.getId()));
            if (yaEsta) continue;

            orden++;
            PlanDetalle detalle = new PlanDetalle();
            detalle.setPlan(plan);
            detalle.setTratamiento(s.tratamiento);
            detalle.setProcedimiento(s.tratamiento.getNombre());
            detalle.setOrden(orden);
            detalle.setPrecio(s.tratamiento.getPrecioReferencia());
            detalle.setEstado(EstadoDetalle.PENDIENTE);
            detalleRepo.save(detalle);
            if (s.tratamiento.getPrecioReferencia() != null) {
                total += s.tratamiento.getPrecioReferencia();
            }
        }

        plan.setCostoEstimado(round2(total));
        repo.save(plan);
        presupuestoService.recalcularCaso(casoId);
        return plan;
    }

    /**
     * Puntua cada tratamiento activo del catalogo segun cuanto coincide con el
     * diagnostico. Siempre incluye la consulta inicial, porque toda atencion
     * empieza con ella; el resto entra solo si el texto lo menciona.
     */
    private List<Sugerencia> sugerirTratamientos(String texto) {
        List<Sugerencia> resultado = new ArrayList<>();

        for (Tratamiento t : tratamientoRepo.activosDe(sesion.odontologoId())) {
            String nombre = normalizar(t.getNombre());
            String categoria = normalizar(t.getCategoria());

            boolean esConsulta = categoria.contains("diagnostico") || nombre.contains("consulta");
            int puntaje = 0;

            if (texto.contains(categoria) && !categoria.isBlank()) {
                puntaje += 6;
            }
            for (String clave : PALABRAS_CLAVE.keySet()) {
                if (texto.contains(clave)) {
                    for (String alias : PALABRAS_CLAVE.get(clave)) {
                        if (nombre.contains(alias) || categoria.contains(alias)) {
                            puntaje += 4;
                        }
                    }
                }
            }
            for (String palabra : palabrasRelevantes(nombre)) {
                if (texto.contains(palabra)) {
                    puntaje += 2;
                }
            }

            if (esConsulta) {
                puntaje = Math.max(puntaje, 8);
            }
            if (puntaje > 0) {
                resultado.add(new Sugerencia(t, puntaje));
            }
        }

        resultado.sort(Comparator
                .comparingInt((Sugerencia s) -> s.puntaje).reversed()
                .thenComparing(s -> s.tratamiento.getNombre()));

        if (resultado.size() > MAX_SUGERENCIAS) {
            resultado = new ArrayList<>(resultado.subList(0, MAX_SUGERENCIAS));
        }
        return resultado;
    }

    private static List<String> palabrasRelevantes(String textoNormalizado) {
        List<String> palabras = new ArrayList<>();
        for (String palabra : textoNormalizado.split("[^a-z0-9]+")) {
            if (palabra.length() >= 5 && !PALABRAS_VACIAS.contains(palabra)) {
                palabras.add(palabra);
            }
        }
        return palabras;
    }

    private static String normalizar(String valor) {
        if (valor == null) return "";
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String join(String a, String b) {
        return (a == null ? "" : a) + " " + (b == null ? "" : b);
    }

    private PlanTratamiento nuevoPlan(CasoClinico caso, String nombre, Integer sesiones) {
        PlanTratamiento plan = new PlanTratamiento();
        plan.setCasoClinico(caso);
        plan.setNombre(nombre);
        plan.setSesionesEstimadas(sesiones);
        plan.setEstado(EstadoPlan.PENDIENTE);
        return plan;
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
