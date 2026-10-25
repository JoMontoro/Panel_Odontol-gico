package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.demo.model.Rol;
import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;

/**
 * Valida el ID token que entrega el boton "Iniciar sesion con Google" y lo
 * convierte en una sesion del panel. Los filtros son:
 *
 *  1. Google debe confirmar que el token es valido, no vencido y emitido por
 *     accounts.google.com (se consulta el tokeninfo oficial).
 *  2. aud debe coincidir con el client id de la aplicacion: impide que alguien
 *     reutilice un token emitido para otra web.
 *  3. email_verified debe venir en true.
 *  4. El correo debe estar registrado en la tabla usuarios con rol ODONTOLOGO
 *     y estado activo: sin registro previo no se puede entrar con Google.
 *  5. Si se configuro un dominio, el gmail debe pertenecer a ese dominio.
 */
@Service
public class GoogleAuthService {

    private static final String TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo";

    @Autowired
    private UsuarioRepository repo;

    private final RestTemplate restTemplate = new RestTemplate();

    private String clienteId = "";
    private String dominioPermitido = "";

    public void configurar(String clienteId, String dominioPermitido) {
        this.clienteId = clienteId == null ? "" : clienteId.trim();
        this.dominioPermitido = dominioPermitido == null ? "" : dominioPermitido.trim();
    }

    public boolean habilitado() {
        return !clienteId.isBlank();
    }

    public String getClienteId() {
        return clienteId;
    }

    public Usuario autenticar(String credential) {
        if (!habilitado()) {
            throw new GoogleAuthException(
                    "El ingreso con Google no esta configurado en el servidor (app.google.client-id vacio).");
        }
        if (credential == null || credential.isBlank()) {
            throw new GoogleAuthException("Google no devolvio ninguna credencial.");
        }

        java.util.Map<String, Object> info = consultarTokenInfo(credential);

        String issuer = str(info.get("iss"));
        if (!("accounts.google.com".equals(issuer) || "https://accounts.google.com".equals(issuer))) {
            throw new GoogleAuthException("La credencial no fue emitida por Google.");
        }

        String aud = str(info.get("aud"));
        if (!clienteId.equals(aud)) {
            throw new GoogleAuthException("La credencial de Google no pertenece a esta aplicacion.");
        }

        String email = str(info.get("email"));
        if (email.isBlank()) {
            throw new GoogleAuthException("Google no devolvio el correo de la cuenta.");
        }

        String verificado = str(info.get("email_verified"));
        if (!"true".equalsIgnoreCase(verificado)) {
            throw new GoogleAuthException("El correo de Google no esta verificado.");
        }

        if (!dominioPermitido.isBlank() && !email.toLowerCase().endsWith("@" + dominioPermitido.toLowerCase())) {
            throw new GoogleAuthException("El correo debe terminar en @" + dominioPermitido + ".");
        }

        long exp = 0L;
        Object expClaim = info.get("exp");
        if (expClaim instanceof Number) {
            exp = ((Number) expClaim).longValue();
        }
        if (exp > 0 && exp * 1000 < System.currentTimeMillis()) {
            throw new GoogleAuthException("La credencial de Google esta vencida. Vuelve a intentarlo.");
        }

        Usuario user = repo.findByCorreoIgnoreCase(email)
                .orElseThrow(() -> new GoogleAuthException(
                        "El correo " + email + " no esta registrado como odontologo. "
                        + "Solicita al administrador que lo agregue."));

        if (user.getRol() != Rol.ODONTOLOGO) {
            throw new GoogleAuthException("Esa cuenta no pertenece a un odontologo del consultorio.");
        }
        if (Boolean.FALSE.equals(user.getEstado())) {
            throw new GoogleAuthException("La cuenta esta desactivada. Contacta al administrador.");
        }

        return user;
    }

    @SuppressWarnings("unchecked")
    private java.util.Map<String, Object> consultarTokenInfo(String credential) {
        try {
            java.util.Map<String, Object> resp = restTemplate.getForObject(
                    TOKENINFO_URL + "?id_token={token}", java.util.Map.class, credential);

            if (resp == null || resp.isEmpty()) {
                throw new GoogleAuthException("Google no pudo validar la credencial.");
            }
            return resp;
        } catch (RestClientException e) {
            throw new GoogleAuthException("No se pudo validar la credencial con Google. Intenta de nuevo.");
        }
    }

    private String str(Object valor) {
        return valor == null ? "" : valor.toString();
    }

    /** Error de autenticacion con Google que el controlador traduce a HTTP 401. */
    public static class GoogleAuthException extends RuntimeException {
        public GoogleAuthException(String mensaje) {
            super(mensaje);
        }
    }
}
