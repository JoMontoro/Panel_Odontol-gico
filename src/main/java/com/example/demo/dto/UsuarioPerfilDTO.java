package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UsuarioPerfilDTO {
    private String username;
    private String nombre;
    private String apellido;
    private String foto;
    private String rol;
}
