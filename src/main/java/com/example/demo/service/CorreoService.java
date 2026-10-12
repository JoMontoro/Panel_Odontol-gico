package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.demo.model.Usuario;

/**
 * Envia el codigo de recuperacion de contrasena usando la cuenta Gmail del
 * consultorio (Gmail SMTP + contrasena de aplicacion). Si el envio falla se
 * avisa al odontologo para que contacte al administrador: nunca se revela si un
 * correo esta registrado.
 */
@Service
public class CorreoService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String remitente;

    @Value("${app.correo.nombre-remitente:Consultorio Odontologico}")
    private String nombreRemitente;

    public boolean habilitado() {
        return mailSender != null && remitente != null && !remitente.isBlank();
    }

    /**
     * @return true si el correo salio, false si no habia SMTP configurado o si
     *         el envio fallo.
     */
    public boolean enviarCodigoRecuperacion(Usuario user, String codigo) {
        if (!habilitado()) {
            System.out.println("[correo] SMTP no configurado: no se pudo avisar a " + user.getCorreo());
            return false;
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(user.getCorreo());
        mensaje.setSubject(nombreRemitente + " - Codigo para recuperar tu contrasena");
        mensaje.setText(mensajeTexto(user, codigo));

        try {
            mailSender.send(mensaje);
            return true;
        } catch (MailException e) {
            System.out.println("[correo] fallo al enviar a " + user.getCorreo() + ": " + e.getMessage());
            return false;
        }
    }

    private String mensajeTexto(Usuario user, String codigo) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hola ").append(user.getNombre()).append(",\n\n");
        sb.append("Solicitaste recuperar la contrasena de tu cuenta del panel odontologo.\n");
        sb.append("Tu usuario es: ").append(user.getUsername()).append("\n");
        sb.append("Tu codigo de verificacion es: ").append(codigo).append("\n\n");
        sb.append("El codigo vence en 15 minutos y puede usarse hasta 5 veces.\n");
        sb.append("Si no fuiste tu, ignora este mensaje: la contrasena no cambia.\n\n");
        sb.append("Correo enviado por ").append(nombreRemitente).append(".\n");
        return sb.toString();
    }
}
