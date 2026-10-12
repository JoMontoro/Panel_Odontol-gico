package com.example.demo.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Lee el token JWT del encabezado Authorization: Bearer ... y reconstruye la
 * sesion. Si el token no existe o no es valido, la peticion sigue el flujo
 * normal (httpBasic), de modo que el login basico previo sigue funcionando.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = resolverToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Claims claims = jwtService.validar(token);
            if (claims != null && claims.getSubject() != null) {
                try {
                    var user = userDetailsService.loadUserByUsername(claims.getSubject());

                    if (user.isEnabled()) {
                        var authentication = new UsernamePasswordAuthenticationToken(
                                user, null, user.getAuthorities());
                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (Exception e) {
                    SecurityContextHolder.clearContext();
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolverToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            return token.isEmpty() ? null : token;
        }
        return null;
    }

    /** Autoridades derivadas del rol, por si se necesitan en otros puntos. */
    public List<SimpleGrantedAuthority> autoridades(String rol) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
    }
}
