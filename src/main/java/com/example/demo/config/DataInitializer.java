package com.example.demo.config;

import com.example.demo.model.Usuario;
import com.example.demo.model.Rol;
import com.example.demo.model.Paciente;
import com.example.demo.model.Tratamiento;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.repository.PacienteRepository;
import com.example.demo.repository.TratamientoRepository;

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

    @Value("${app.odontologos.dr-gina.correo:gina.odontologia@gmail.com}")
    private String correoGina;

    @Value("${app.odontologos.dr-ruben.correo:ruben.odontologia@gmail.com}")
    private String correoRuben;

    @Bean
    CommandLineRunner initUsuarios(UsuarioRepository repo, PasswordEncoder encoder) {
        return args -> {
            asegurarOdontologo(repo, encoder, "Dr.Gina", "odontologo", "Gina", "Reupo",
                    correoGina, "Odontología General", "Gina");
            asegurarOdontologo(repo, encoder, "Dr.Ruben", "diego", "Ruben", "Cullanco",
                    correoRuben, "Ortodoncia", "Diego");
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

            Usuario gina = usuarioRepo.findByUsername("Dr.Gina").orElse(null);
            if (gina == null) return;

            sinDueno.forEach(p -> p.setOdontologo(gina));
            pacienteRepo.saveAll(sinDueno);
            System.out.println("Cartera inicial: " + sinDueno.size()
                    + " paciente(s) asignados a " + gina.getNombre() + " " + gina.getApellido());
        };
    }

    @Bean
    CommandLineRunner initTratamientos(TratamientoRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                // ENDODONCIA
                repo.save(new Tratamiento(null, "ENDO-01", "Endodoncia unirradicular", "Tratamiento de conducto en diente con una raíz", 60, 150.0, "ENDODONCIA", true));
                repo.save(new Tratamiento(null, "ENDO-02", "Endodoncia birradicular", "Tratamiento de conducto en diente con dos raíces", 90, 200.0, "ENDODONCIA", true));
                repo.save(new Tratamiento(null, "ENDO-03", "Endodoncia multirradicular", "Tratamiento de conducto en diente con tres o más raíces", 120, 280.0, "ENDODONCIA", true));
                repo.save(new Tratamiento(null, "ENDO-04", "Retratamiento endodóntico", "Reintervención de conducto previo", 120, 300.0, "ENDODONCIA", true));

                // OPERATORIA / RESTAURACIÓN
                repo.save(new Tratamiento(null, "OPE-01", "Restauración resina clase I", "Obturación simple en cara oclusal", 30, 45.0, "OPERATORIA", true));
                repo.save(new Tratamiento(null, "OPE-02", "Restauración resina clase II", "Obturación en cara oclusal y proximal", 45, 60.0, "OPERATORIA", true));
                repo.save(new Tratamiento(null, "OPE-03", "Restauración resina clase III/IV", "Obturación en cara proximal anterior", 45, 65.0, "OPERATORIA", true));
                repo.save(new Tratamiento(null, "OPE-04", "Restauración resina clase V", "Obturación en cuello dental", 30, 50.0, "OPERATORIA", true));
                repo.save(new Tratamiento(null, "OPE-05", "Incrustación cerámica", "Inlay/Onlay cerámico indirecto", 90, 350.0, "OPERATORIA", true));
                repo.save(new Tratamiento(null, "OPE-06", "Corona cerámica", "Corona total cerámica (zirconio/disilicato)", 120, 450.0, "OPERATORIA", true));
                repo.save(new Tratamiento(null, "OPE-07", "Corona metal-porcelana", "Corona total metalocerámica", 120, 380.0, "OPERATORIA", true));

                // PERIODONCIA
                repo.save(new Tratamiento(null, "PER-01", "Profilaxis dental", "Limpieza dental completa", 45, 50.0, "PERIODONCIA", true));
                repo.save(new Tratamiento(null, "PER-02", "Raspado y alisado radicular", "Por cuadrante", 60, 80.0, "PERIODONCIA", true));
                repo.save(new Tratamiento(null, "PER-03", "Cirugía periodontal", "Colgajo / regeneración guiada", 90, 250.0, "PERIODONCIA", true));
                repo.save(new Tratamiento(null, "PER-04", "Injerto gingival", "Recesión gingival", 60, 200.0, "PERIODONCIA", true));

                // CIRUGÍA ORAL
                repo.save(new Tratamiento(null, "CIR-01", "Exodoncia simple", "Extracción dental simple", 30, 40.0, "CIRUGIA", true));
                repo.save(new Tratamiento(null, "CIR-02", "Exodoncia compleja / quirúrgica", "Extracción con ostectomía / odontosección", 60, 120.0, "CIRUGIA", true));
                repo.save(new Tratamiento(null, "CIR-03", "Exodoncia de cordal", "Extracción de tercer molar", 60, 150.0, "CIRUGIA", true));
                repo.save(new Tratamiento(null, "CIR-04", "Alveoloplastia", "Regularización de reborde alveolar", 45, 100.0, "CIRUGIA", true));

                // PRÓTESIS
                repo.save(new Tratamiento(null, "PRO-01", "Prótesis removible acrílica", "Parcial o total", 120, 400.0, "PROTESIS", true));
                repo.save(new Tratamiento(null, "PRO-02", "Prótesis removible metálica (esquelética)", "Con armazón metálico", 120, 600.0, "PROTESIS", true));
                repo.save(new Tratamiento(null, "PRO-03", "Puente fijo 3 unidades", "Metal-porcelana o cerámica", 180, 900.0, "PROTESIS", true));
                repo.save(new Tratamiento(null, "PRO-04", "Prótesis sobre implante", "Unitario / puente", 120, 800.0, "PROTESIS", true));

                // ORTODONCIA
                repo.save(new Tratamiento(null, "ORT-01", "Ortodoncia brackets metálicos", "Tratamiento completo", 0, 2500.0, "ORTODONCIA", true));
                repo.save(new Tratamiento(null, "ORT-02", "Ortodoncia brackets cerámicos", "Tratamiento completo", 0, 3200.0, "ORTODONCIA", true));
                repo.save(new Tratamiento(null, "ORT-03", "Ortodoncia autoligable", "Tratamiento completo", 0, 3500.0, "ORTODONCIA", true));
                repo.save(new Tratamiento(null, "ORT-04", "Alineadores transparentes", "Tratamiento completo (ej. Invisalign)", 0, 4000.0, "ORTODONCIA", true));
                repo.save(new Tratamiento(null, "ORT-05", "Retenedores fijos/removibles", "Post-tratamiento", 30, 150.0, "ORTODONCIA", true));

                // IMPLANTES
                repo.save(new Tratamiento(null, "IMP-01", "Implante dental unitario", "Incluye tornillo + corona", 120, 1200.0, "IMPLANTES", true));
                repo.save(new Tratamiento(null, "IMP-02", "Injerto óseo", "Regeneración ósea guiada", 90, 300.0, "IMPLANTES", true));
                repo.save(new Tratamiento(null, "IMP-03", "Elevación de seno maxilar", "Técnica lateral / crestal", 90, 400.0, "IMPLANTES", true));

                // BLANQUEAMIENTO / ESTÉTICA
                repo.save(new Tratamiento(null, "EST-01", "Blanqueamiento en consultorio", "LED / Láser", 90, 250.0, "ESTETICA", true));
                repo.save(new Tratamiento(null, "EST-02", "Blanqueamiento ambulatorio", "Férulas + gel", 30, 180.0, "ESTETICA", true));
                repo.save(new Tratamiento(null, "EST-03", "Carillas cerámicas", "Por unidad", 120, 400.0, "ESTETICA", true));
                repo.save(new Tratamiento(null, "EST-04", "Microabrasión / Icon", "Lesiones blancas", 45, 80.0, "ESTETICA", true));

                // DIAGNÓSTICO / PREVENTIVO
                repo.save(new Tratamiento(null, "DIA-01", "Consulta inicial / diagnóstico", "Examen clínico + radiografías", 30, 30.0, "DIAGNOSTICO", true));
                repo.save(new Tratamiento(null, "DIA-02", "Radiografía periapical", "Por placa", 10, 15.0, "DIAGNOSTICO", true));
                repo.save(new Tratamiento(null, "DIA-03", "Radiografía panorámica", "Ortopantomografía", 15, 40.0, "DIAGNOSTICO", true));
                repo.save(new Tratamiento(null, "DIA-04", "Tomografía CBCT", "Cone Beam CT", 20, 120.0, "DIAGNOSTICO", true));
                repo.save(new Tratamiento(null, "DIA-05", "Sellante de fosas y fisuras", "Por diente", 20, 25.0, "PREVENTIVO", true));
                repo.save(new Tratamiento(null, "DIA-06", "Aplicación de flúor", "Tópico profesional", 15, 20.0, "PREVENTIVO", true));

                System.out.println("✔ Catálogo de tratamientos cargado (" + repo.count() + " tratamientos)");
            }
        };
    }
}
