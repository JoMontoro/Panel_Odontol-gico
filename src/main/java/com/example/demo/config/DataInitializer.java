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
            if (repo.findByUsername("odontologo").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setUsername("odontologo");
                admin.setPassword(encoder.encode("123456"));
                admin.setNombre("Gina");
                admin.setApellido("Reupo");
                admin.setCorreo("gina@clinica.com");
                admin.setEspecialidad("Odontología General");
                admin.setRol(Rol.ODONTOLOGO);
                repo.save(admin);

                Usuario odontologo2 = new Usuario();
                odontologo2.setUsername("diego");
                odontologo2.setPassword(encoder.encode("123456"));
                odontologo2.setNombre("Diego");
                odontologo2.setApellido("Cullanco");
                odontologo2.setCorreo("diego@clinica.com");
                odontologo2.setEspecialidad("Ortodoncia");
                odontologo2.setRol(Rol.ODONTOLOGO);
                repo.save(odontologo2);

                System.out.println("✔ Odontólogos creados automáticamente");
            }
        };
    }
}
