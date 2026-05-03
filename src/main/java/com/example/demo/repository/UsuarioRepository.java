

package com.example.demo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

import com.example.demo.model.Usuario;
import com.example.demo.model.Rol;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional <Usuario> findByUsername(String username);
    List<Usuario> findByRol(Rol rol);
}
