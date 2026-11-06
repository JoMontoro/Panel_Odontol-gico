package com.example.demo.config;

import com.example.demo.model.Cita;
import com.example.demo.model.Paciente;
import com.example.demo.model.PlanDetalle;
import com.example.demo.model.Rol;
import com.example.demo.model.Tratamiento;
import com.example.demo.model.Usuario;
import com.example.demo.repository.CitaRepository;
import com.example.demo.repository.PacienteRepository;
import com.example.demo.repository.PlanDetalleRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.repository.TratamientoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    /** Contrasena inicial de los dos odontologos: 123456 (luego se puede cambiar). */
    private static final String CLAVE_INICIAL = "123456";

    /** Usuario del odontologo que hereda el historial anterior al aislamiento por catalogo. */
    private static final String USUARIO_GINA = "Dr.Gina";

    @Value("${app.odontologos.dr-gina.correo:gina.odontologia@gmail.com}")
    private String correoGina;

    @Value("${app.odontologos.dr-ruben.correo:ruben.odontologia@gmail.com}")
    private String correoRuben;

    @Bean
    CommandLineRunner initUsuarios(UsuarioRepository repo, PasswordEncoder encoder) {
        return args -> {
            asegurarOdontologo(repo, encoder, "Dr.Gina", "odontologo", "Gina", "Reupo",
                    correoGina, "Odontología General", "Gina");
            asegurarOdontologo(repo, encoder, "Dr.Ruben", "ruben", "Ruben", "Montalvo",
                    correoRuben, "Ortodoncia", "Ruben");
        };
    }

    /**
     * Deja al odontologo con el usuario "nuevoUsuario". Si todavia existe con el
     * usuario anterior ("anteriorUsuario") se renombra en vez de crear otro, para
     * conservar el id y la cartera de pacientes ya asignada.
     */
    private void asegurarOdontologo(UsuarioRepository repo, PasswordEncoder encoder,
            String nuevoUsuario, String anteriorUsuario, String nombre, String apellido,
            String correo, String especialidad, String nombreAnterior) {

        Optional<Usuario> porNuevo = repo.findByUsername(nuevoUsuario);

        if (porNuevo.isPresent()) {
            sincronizarDatos(repo, porNuevo.get(), nombre, apellido, correo, especialidad,
                    nuevoUsuario, nombreAnterior);
            return;
        }

        Optional<Usuario> porAnterior = repo.findByUsername(anteriorUsuario);

        if (porAnterior.isPresent()) {
            Usuario user = porAnterior.get();
            System.out.println("Credenciales migradas: '" + anteriorUsuario + "' -> '" + nuevoUsuario + "'");
            user.setUsername(nuevoUsuario);
            sincronizarDatos(repo, user, nombre, apellido, correo, especialidad,
                    nuevoUsuario, nombreAnterior);
            repo.save(user);
            return;
        }

        Usuario nuevo = new Usuario();
        nuevo.setUsername(nuevoUsuario);
        nuevo.setPassword(encoder.encode(CLAVE_INICIAL));
        nuevo.setNombre(nombre);
        nuevo.setApellido(apellido);
        nuevo.setCorreo(correo);
        nuevo.setEspecialidad(especialidad);
        nuevo.setRol(Rol.ODONTOLOGO);
        nuevo.setEstado(true);
        repo.save(nuevo);

        System.out.println("Odontólogo creado: " + nuevoUsuario
                + " (" + correo + ") con clave inicial " + CLAVE_INICIAL);
    }

    /**
     * Completa los datos que falten (correo, especialidad, rol, estado) pero no
     * pisa la contrasena ya cambiada ni el Gmail real que el odontologo
     * registro desde su perfil. Si el correo sigue siendo el viejo @clinica.com
     * (dato de ejemplo), si se reemplaza por el Gmail configurado: sin eso el
     * ingreso con Google y el codigo de recuperacion nunca funcionarian.
     */
    private void sincronizarDatos(UsuarioRepository repo, Usuario user, String nombre, String apellido,
            String correo, String especialidad, String usuario, String nombreAnterior) {

        boolean cambio = false;

        if (nombreAnterior != null && nombreAnterior.equalsIgnoreCase(user.getNombre())) {
            user.setNombre(nombre);
            cambio = true;
        }
        if (esVacio(user.getNombre())) {
            user.setNombre(nombre);
            cambio = true;
        }
        if (esVacio(user.getApellido())) {
            user.setApellido(apellido);
            cambio = true;
        }
        if (esVacio(user.getCorreo()) || esCorreoDeEjemplo(user.getCorreo())) {
            user.setCorreo(correo);
            cambio = true;
        }
        if (esVacio(user.getEspecialidad())) {
            user.setEspecialidad(especialidad);
            cambio = true;
        }
        if (user.getRol() == null) {
            user.setRol(Rol.ODONTOLOGO);
            cambio = true;
        }
        if (user.getEstado() == null) {
            user.setEstado(true);
            cambio = true;
        }

        if (cambio) {
            repo.save(user);
            System.out.println("Datos actualizados de " + usuario + " (correo: " + user.getCorreo() + ")");
        }
    }

    /** Los correos @clinica.com eran de ejemplo y no sirven para Gmail. */
    private boolean esCorreoDeEjemplo(String correo) {
        return correo != null && correo.toLowerCase().endsWith("@clinica.com");
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    /**
     * Los pacientes que ya existian antes del aislamiento por odontologo no
     * tienen dueno. Se quedan con Gina (usuario "Dr.Gina") y la asignacion es
     * idempotente: en los siguientes arranques no vuelve a tocar nada.
     */
    @Bean
    CommandLineRunner asignarDuenoPacientes(PacienteRepository pacienteRepo, UsuarioRepository usuarioRepo) {
        return args -> {
            List<Paciente> sinDueno = pacienteRepo.findByOdontologoIsNull();
            if (sinDueno.isEmpty()) return;

            Usuario gina = usuarioRepo.findByUsername(USUARIO_GINA).orElse(null);
            if (gina == null) return;

            sinDueno.forEach(p -> p.setOdontologo(gina));
            pacienteRepo.saveAll(sinDueno);
            System.out.println("Cartera inicial: " + sinDueno.size()
                    + " paciente(s) asignados a " + gina.getNombre() + " " + gina.getApellido());
        };
    }

    @Bean
    CommandLineRunner limpiarCatalogoTratamientos(TratamientoRepository tratamientoRepo,
            PlanDetalleRepository planDetalleRepo, CitaRepository citaRepo,
            PacienteRepository pacienteRepo, UsuarioRepository usuarioRepo) {
        return args -> {
            List<Tratamiento> sinDueno = tratamientoRepo.findByOdontologoIsNull();
            if (sinDueno.isEmpty()) return;

            List<Tratamiento> sinUso = new ArrayList<>();
            int asignados = 0;

            for (Tratamiento t : sinDueno) {
                if (!planDetalleRepo.existsByTratamiento_Id(t.getId())
                        && !citaRepo.existsByTratamiento_Id(t.getId())) {
                    sinUso.add(t);
                    continue;
                }

                // Ya esta en el historial de algun odontologo: se asigna a quien lo
                // uso para no romper citas, planes ni presupuestos existentes.
                Usuario dueno = odontologoQueUso(t, planDetalleRepo, citaRepo, pacienteRepo);
                t.setOdontologo(dueno != null ? dueno
                        : usuarioRepo.findByUsername(USUARIO_GINA).orElse(null));
                if (t.getOdontologo() != null) {
                    tratamientoRepo.save(t);
                    asignados++;
                }
            }

            if (!sinUso.isEmpty()) {
                tratamientoRepo.deleteAll(sinUso);
            }

            System.out.println("Catalogo de tratamientos depurado: " + sinUso.size()
                    + " tratamiento(s) de ejemplo eliminados, " + asignados
                    + " conservados por historial. Cada odontologo registra ahora los suyos.");
        };
    }

    /**
     * Odontologo dueno del paciente sobre el que se uso el tratamiento; sirve de
     * respaldo cuando la cita no guardo el odontologo responsable.
     */
    private Usuario odontologoQueUso(Tratamiento tratamiento, PlanDetalleRepository planDetalleRepo,
            CitaRepository citaRepo, PacienteRepository pacienteRepo) {
        for (PlanDetalle d : planDetalleRepo.findByTratamiento_Id(tratamiento.getId())) {
            Paciente p = d.getPlan() != null && d.getPlan().getCasoClinico() != null
                    ? d.getPlan().getCasoClinico().getPaciente() : null;
            if (p != null && p.getOdontologo() != null) return p.getOdontologo();
        }
        for (Cita c : citaRepo.findByTratamiento_Id(tratamiento.getId())) {
            Usuario odo = c.getOdontologo() != null ? c.getOdontologo() : null;
            if (odo != null) return odo;
            if (c.getPaciente() != null && c.getPaciente().getOdontologo() != null) {
                return c.getPaciente().getOdontologo();
            }
        }
        return null;
    }
}
