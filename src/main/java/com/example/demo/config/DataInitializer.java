package com.example.demo.config;

import com.example.demo.model.Usuario;
import com.example.demo.model.Rol;
import com.example.demo.repository.UsuarioRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsuarios(UsuarioRepository repo, PasswordEncoder encoder) {
        return args -> {

            if (repo.findByRol(Rol.ODONTOLOGO).isEmpty()) {

                Usuario admin = new Usuario();
                admin.setUsername("odontologo");
                admin.setPassword(encoder.encode("123456"));
                admin.setRol(Rol.ODONTOLOGO);

                repo.save(admin);

                System.out.println("✔ Odontólogo creado automáticamente");
            }
        };
    }
}