package com.example.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Codigo de 6 digitos que se envia al Gmail registrado del odontologo cuando
 * olvida su contrasena. Vive 15 minutos y admite un maximo de intentos para
 * que el codigo no se pueda adivinar por fuerza bruta.
 */
@Entity
@Table(name = "recuperacion_password")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecuperacionPassword {

    public static final int INTENTOS_MAXIMOS = 5;
    public static final int MINUTOS_VIGENCIA = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String correo;

    @Column(nullable = false, length = 6)
    private String codigo;

    private Integer intentos;

    private Boolean usado;

    private LocalDateTime creadoEn;

    private LocalDateTime expiraEn;

    @PrePersist
    public void prePersist() {
        if (intentos == null) intentos = 0;
        if (usado == null) usado = false;
        if (creadoEn == null) creadoEn = LocalDateTime.now();
        if (expiraEn == null) expiraEn = creadoEn.plusMinutes(MINUTOS_VIGENCIA);
    }

    public boolean expirado() {
        return expiraEn != null && expiraEn.isBefore(LocalDateTime.now());
    }

    public boolean disponible() {
        return Boolean.FALSE.equals(usado) && !expirado()
                && intentos != null && intentos < INTENTOS_MAXIMOS;
    }
}
