

package com.example.demo.service;
import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;



@Service
public class CustomUserDetailsService implements UserDetailsService{

    @Autowired
    private UsuarioRepository repo;

    @Override
    public UserDetails loadUserByUsername(String username) {
        Usuario user = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRol().name())
                .disabled(Boolean.FALSE.equals(user.getEstado()))
                .build();
    }
}
