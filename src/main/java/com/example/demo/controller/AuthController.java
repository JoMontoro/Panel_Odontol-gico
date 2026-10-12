package com.example.demo.controller;

import com.example.demo.dto.UsuarioPerfilDTO;
import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.CorreoService;
import com.example.demo.service.GoogleAuthService;
import com.example.demo.service.JwtService;
import com.example.demo.service.RecuperacionPasswordService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private GoogleAuthService googleAuthService;

    @Autowired
    private RecuperacionPasswordService recuperacionService;

    @Autowired
    private CorreoService correoService;

    private String uploadDir;

    @PostConstruct
    public void init() {
        uploadDir = System.getProperty("user.dir") + "/uploads/profiles";
    }

    /**
     * Configuracion publica que el frontend necesita antes de mostrar el boton
     * de Google: el client id y si el correo de recuperacion esta activo.
     */
    @GetMapping("/config")
    public Map<String, Object> config() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("googleClienteId", googleAuthService.getClienteId());
        m.put("googleActivo", googleAuthService.habilitado());
        m.put("correoActivo", correoService.habilitado());
        m.put("expiracionHoras", jwtService.getExpiracionHoras());
        return m;
    }

    /** Login con usuario y contrasena (Dr.Gina / Dr.Ruben + 123456). */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username") == null ? "" : body.get("username").trim();
        String password = body.get("password") == null ? "" : body.get("password");

        if (username.isBlank() || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Escribe tu usuario y tu contrasena.");
        }

        Usuario user = repo.findByUsername(username).orElse(null);

        if (user == null || !encoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Usuario o contrasena incorrectos.");
        }
        if (Boolean.FALSE.equals(user.getEstado())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Tu cuenta esta desactivada. Contacta al administrador.");
        }

        return ResponseEntity.ok(respuestaSesion(user, "CLAVE"));
    }

    /**
     * Login con Google: el frontend manda el credential (ID token) que entrega
     * el boton oficial. El servicio valida el token y exige que el correo este
     * registrado como odontologo activo.
     */
    @PostMapping("/google")
    public ResponseEntity<Map<String, Object>> loginGoogle(@RequestBody Map<String, String> body) {
        String credential = body.get("credential");

        Usuario user;
        try {
            user = googleAuthService.autenticar(credential);
        } catch (GoogleAuthService.GoogleAuthException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }

        Map<String, Object> respuesta = respuestaSesion(user, "GOOGLE");
        respuesta.put("correo", user.getCorreo());
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Paso 1 de "olvide mi contrasena": manda el codigo de 6 digitos al Gmail
     * registrado. La respuesta es la misma exista o no el correo.
     */
    @PostMapping("/recuperar-password")
    public Map<String, Object> recuperarPassword(@RequestBody Map<String, String> body) {
        RecuperacionPasswordService.Resultado r = recuperacionService.solicitar(body.get("correo"));

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("mensaje", r.mensaje());

        if (r.correoEnviado()) {
            m.put("correoEnviado", true);
        } else if (!correoService.habilitado()) {
            m.put("correoEnviado", false);
            m.put("detalle", "El envio de correos no esta configurado en el servidor. "
                    + "Pide al administrador que configure el Gmail del consultorio.");
        } else {
            m.put("correoEnviado", false);
            m.put("detalle", "No se pudo enviar el correo en este momento. Intenta de nuevo en 1 minuto.");
        }

        return m;
    }

    /** Paso 2: valida el codigo y confirma que el correo corresponde a un odontologo. */
    @PostMapping("/verificar-codigo")
    public Map<String, Object> verificarCodigo(@RequestBody Map<String, String> body) {
        Usuario user = recuperacionService.verificarCodigo(body.get("correo"), body.get("codigo"));

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("mensaje", "Codigo correcto. Define tu nueva contrasena.");
        m.put("username", user.getUsername());
        return m;
    }

    /** Paso 3: guarda la nueva contrasena e invalida los codigos anteriores. */
    @PostMapping("/restablecer-password")
    public Map<String, Object> restablecerPassword(@RequestBody Map<String, String> body) {
        recuperacionService.restablecer(body.get("correo"), body.get("codigo"), body.get("nuevaPassword"));

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("mensaje", "Contrasena actualizada. Ya puedes iniciar sesion.");
        return m;
    }

    /**
     * Alta de usuarios. Se mantiene por compatibilidad, pero ya no es publica:
     * solo un odontologo autenticado puede crear cuentas.
     */
    @PostMapping("/register")
    public Usuario register(@RequestBody Usuario user) {
        if (user.getCorreo() != null && !user.getCorreo().isBlank()
                && repo.existsByCorreoIgnoreCase(user.getCorreo().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ese correo ya esta registrado.");
        }
        user.setCorreo(user.getCorreo() == null ? null : user.getCorreo().trim());
        user.setPassword(encoder.encode(user.getPassword()));
        return repo.save(user);
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioPerfilDTO> perfil(Authentication auth) {
        String username = auth.getName();
        Usuario user = repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UsuarioPerfilDTO dto = new UsuarioPerfilDTO(
                user.getUsername(),
                user.getNombre(),
                user.getApellido(),
                user.getFoto(),
                user.getRol().name()
        );

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/me")
    public ResponseEntity<UsuarioPerfilDTO> actualizarPerfil(
            Authentication auth,
            @RequestBody Map<String, String> body) {

        Usuario user = repo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (body.containsKey("nombre")) {
            user.setNombre(body.get("nombre"));
        }
        if (body.containsKey("apellido")) {
            user.setApellido(body.get("apellido"));
        }

        repo.save(user);

        UsuarioPerfilDTO dto = new UsuarioPerfilDTO(
                user.getUsername(),
                user.getNombre(),
                user.getApellido(),
                user.getFoto(),
                user.getRol().name()
        );

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/foto")
    public ResponseEntity<Map<String, String>> subirFoto(
            Authentication auth,
            @RequestParam("file") MultipartFile file) throws IOException {

        Usuario user = repo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);

        String ext = file.getOriginalFilename();
        ext = ext != null && ext.contains(".") ? ext.substring(ext.lastIndexOf(".")) : ".jpg";
        String filename = UUID.randomUUID().toString() + ext;
        Path filepath = dir.resolve(filename);
        Files.copy(file.getInputStream(), filepath);

        String url = "/uploads/profiles/" + filename;
        user.setFoto(url);
        repo.save(user);

        return ResponseEntity.ok(Map.of("foto", url));
    }

    private Map<String, Object> respuestaSesion(Usuario user, String metodo) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("token", jwtService.generar(user));
        m.put("metodo", metodo);
        m.put("perfil", new UsuarioPerfilDTO(
                user.getUsername(),
                user.getNombre(),
                user.getApellido(),
                user.getFoto(),
                user.getRol().name()
        ));
        return m;
    }
}
