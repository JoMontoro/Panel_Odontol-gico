package com.example.demo.service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.model.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Emite y valida el token de sesion (JWT) que devuelve /api/auth/login y
 * /api/auth/google. El subject es el username, igual que en la autenticacion
 * basica, para que SesionActual siga resolviendo al odontologo sin cambios.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expiracionHoras;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiracion-horas:8}") long expiracionHoras) {

        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "app.jwt.secret debe tener al menos 32 caracteres para firmar el token.");
        }
        Key k = Keys.hmacShaKeyFor(bytes);
        this.key = (SecretKey) k;
        this.expiracionHoras = expiracionHoras;
    }

    public String generar(Usuario user) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + expiracionHoras * 3600_000L);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("rol", user.getRol() != null ? user.getRol().name() : null)
                .claim("correo", user.getCorreo())
                .setIssuedAt(ahora)
                .setExpiration(expira)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Devuelve los claims si el token es valido y sigue vigente, o null. */
    public Claims validar(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean esValido(String token) {
        return validar(token) != null;
    }

    public long getExpiracionHoras() {
        return expiracionHoras;
    }
}
