package com.example.demo.controller;

import com.example.demo.dto.UsuarioPerfilDTO;
import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private PasswordEncoder encoder;

    private String uploadDir;

    @PostConstruct
    public void init() {
        uploadDir = System.getProperty("user.dir") + "/uploads/profiles";
    }

    @PostMapping("/register")
    public Usuario register(@RequestBody Usuario user) {
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
}
