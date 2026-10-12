package com.example.demo.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.model.RecuperacionPassword;
import com.example.demo.model.Rol;
import com.example.demo.model.Usuario;
import com.example.demo.repository.RecuperacionPasswordRepository;
import com.example.demo.repository.UsuarioRepository;

/**
 * Flujo "olvide mi contrasena": el odontologo escribe su Gmail, recibe un
 * codigo de 6 digitos, lo verifica y define una nueva contrasena.
 *
 * Reglas de seguridad:
 *  - la respuesta es la misma exista o no el correo (no se filtran cuentas);
 *  - solo se admite un envio cada 60 segundos por correo;
 *  - el codigo vence en 15 minutos y falla tras 5 intentos;
 *  - al restablecer, se anulan todos los codigos anteriores del correo.
 */
@Service
public class RecuperacionPasswordService {

    private static final long SEGUNDOS_ENTRE_ENVIOS = 60;
    private static final int INTENTOS_MAXIMOS = RecuperacionPassword.INTENTOS_MAXIMOS;
    private static final int LONGITUD_MINIMA = 6;

    private final SecureRandom random = new SecureRandom();
    private final Map<String, LocalDateTime> ultimoEnvio = new ConcurrentHashMap<>();

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private RecuperacionPasswordRepository repo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private CorreoService correoService;

    public Resultado solicitar(String correoIngresado) {
        String correo = normalizar(correoIngresado);

        if (!formatoValido(correo)) {
            return new Resultado(generico(), false);
        }

        Usuario user = usuarioRepo.findByCorreoIgnoreCase(correo).orElse(null);

        boolean esOdontologo = user != null
                && user.getRol() == Rol.ODONTOLOGO
                && !Boolean.FALSE.equals(user.getEstado());

        if (user == null || !esOdontologo) {
            return new Resultado(generico(), false);
        }

        if (!puedeReenviar(correo)) {
            return new Resultado(generico(), false);
        }

        repo.deleteByCorreoIgnoreCaseAndUsadoFalse(correo);
        repo.flush();

        RecuperacionPassword token = new RecuperacionPassword();
        token.setUsername(user.getUsername());
        token.setCorreo(user.getCorreo());
        token.setCodigo(generarCodigo());
        token.setIntentos(0);
        token.setUsado(false);
        token.setCreadoEn(LocalDateTime.now());
        token.setExpiraEn(LocalDateTime.now().plusMinutes(RecuperacionPassword.MINUTOS_VIGENCIA));
        repo.save(token);

        ultimoEnvio.put(correo, LocalDateTime.now());

        boolean enviado = correoService.enviarCodigoRecuperacion(user, token.getCodigo());
        System.out.println("[recuperacion] codigo generado para " + user.getUsername()
                + (enviado ? " (correo enviado)" : " (NO se pudo enviar el correo)"));

        return new Resultado(generico(), enviado);
    }

    public Usuario verificarCodigo(String correoIngresado, String codigoIngresado) {
        RecuperacionPassword token = vigente(normalizar(correoIngresado));

        if (!coincide(token, codigoIngresado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El codigo no es correcto o ya vencio. Solicita uno nuevo.");
        }

        return usuarioRepo.findByUsername(token.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El codigo ya no es valido."));
    }

    public void restablecer(String correoIngresado, String codigoIngresado, String nuevaPassword) {
        validarNuevaPassword(nuevaPassword);

        String correo = normalizar(correoIngresado);
        RecuperacionPassword token = vigente(correo);

        if (!coincide(token, codigoIngresado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El codigo no es correcto o ya vencio. Solicita uno nuevo.");
        }

        Usuario user = usuarioRepo.findByUsername(token.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El codigo ya no es valido."));

        user.setPassword(encoder.encode(nuevaPassword));
        usuarioRepo.save(user);

        token.setUsado(true);
        repo.save(token);
        repo.deleteByCorreoIgnoreCaseAndUsadoFalse(correo);

        System.out.println("[recuperacion] contrasena actualizada para " + user.getUsername());
    }

    private String generico() {
        return "Si el correo pertenece a un odontologo del consultorio, "
                + "recibiras un codigo de 6 digitos en unos segundos.";
    }

    private String normalizar(String correo) {
        return correo == null ? "" : correo.trim();
    }

    private boolean formatoValido(String correo) {
        return correo.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean puedeReenviar(String correo) {
        LocalDateTime anterior = ultimoEnvio.get(correo);
        return anterior == null
                || anterior.plusSeconds(SEGUNDOS_ENTRE_ENVIOS).isBefore(LocalDateTime.now());
    }

    private RecuperacionPassword vigente(String correo) {
        RecuperacionPassword token = repo.findFirstByCorreoIgnoreCaseAndUsadoFalseOrderByIdDesc(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No hay ningun codigo vigente para ese correo. Solicita uno nuevo."));

        if (token.getIntentos() != null && token.getIntentos() >= INTENTOS_MAXIMOS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El codigo se agoto tras " + INTENTOS_MAXIMOS + " intentos. Solicita uno nuevo.");
        }
        if (token.expirado()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El codigo vencio. Solicita uno nuevo.");
        }
        return token;
    }

    private boolean coincide(RecuperacionPassword token, String codigoIngresado) {
        String codigo = codigoIngresado == null ? "" : codigoIngresado.trim();

        if (!token.getCodigo().equals(codigo)) {
            token.setIntentos(token.getIntentos() + 1);
            repo.save(token);
            return false;
        }
        return true;
    }

    private void validarNuevaPassword(String nuevaPassword) {
        if (nuevaPassword == null || nuevaPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Escribe la nueva contrasena.");
        }
        if (nuevaPassword.length() < LONGITUD_MINIMA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contrasena debe tener al menos " + LONGITUD_MINIMA + " caracteres.");
        }
        if (nuevaPassword.contains(" ")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contrasena no puede tener espacios.");
        }
    }

    private String generarCodigo() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    /** Resultado de solicitar el codigo: nunca revela si el correo existe. */
    public record Resultado(String mensaje, boolean correoEnviado) {
    }
}
