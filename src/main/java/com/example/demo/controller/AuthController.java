

package com.example.demo.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;




@RestController
@RequestMapping("api/auth")
public class AuthController {

    
    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private PasswordEncoder encoder;

    @PostMapping("/register")
    public Usuario register(@RequestBody Usuario user) {
        user.setPassword(encoder.encode(user.getPassword()));
        return repo.save(user);
    }
}
