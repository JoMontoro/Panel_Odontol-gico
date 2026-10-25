package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import jakarta.servlet.DispatcherType;

import java.util.List;

import com.example.demo.service.GoogleAuthService;
import com.example.demo.service.JwtAuthenticationFilter;

@Configuration
public class securityconfig {

    @Value("${app.google.client-id:}")
    private String googleClientId;

    @Value("${app.google.dominio-permitido:}")
    private String googleDominioPermitido;

    private final GoogleAuthService googleAuthService;

    public securityconfig(GoogleAuthService googleAuthService) {
        this.googleAuthService = googleAuthService;
        googleAuthService.configurar(googleClientId, googleDominioPermitido);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .authorizeHttpRequests(auth -> auth
                /* El dispatch de error de Spring Boot vuelve a entrar por /error:
                   sin esto, cualquier 400/404/405 terminaba en 401. */
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers("/error").permitAll()
                /* Login y recuperacion de contrasena: publicas a proposito.
                   El propio servicio aplica los filtros (gmail registrado,
                   rol ODONTOLOGO, codigo de un solo uso, limite de intentos). */
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/google").permitAll()
                .requestMatchers("/api/auth/config").permitAll()
                .requestMatchers("/api/auth/recuperar-password").permitAll()
                .requestMatchers("/api/auth/verificar-codigo").permitAll()
                .requestMatchers("/api/auth/restablecer-password").permitAll()
                .requestMatchers("/api/auth/register").hasRole("ODONTOLOGO")
                .requestMatchers("/api/pacientes/**").hasRole("ODONTOLOGO")
                .requestMatchers("/api/casos/**").hasRole("ODONTOLOGO")
                .requestMatchers("/api/citas/**").hasRole("ODONTOLOGO")
                .requestMatchers("/api/dashboard/**").hasRole("ODONTOLOGO")
                .requestMatchers("/api/historial/**").hasRole("ODONTOLOGO")
                .requestMatchers("/api/odontogramas/**").hasRole("ODONTOLOGO")
                .requestMatchers("/api/presupuestos/**").hasRole("ODONTOLOGO")
                /* El catalogo es propio de cada odontologo: el ADMIN supervisor tambien
                   puede consultarlo (solo lectura), igual que las demas carteras. */
                .requestMatchers("/api/tratamientos/**").hasAnyRole("ODONTOLOGO", "ADMIN")
                .requestMatchers("/api/planes-tratamiento/**").hasRole("ODONTOLOGO")
                .requestMatchers("/api/verificaciones/**").hasRole("ODONTOLOGO")
                .requestMatchers("/uploads/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
