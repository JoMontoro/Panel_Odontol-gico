package com.example.demo.dto;

import com.example.demo.model.TipoProcedimiento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Peticion para guardar/aceptar un consentimiento informado.
 * La firma del paciente viaja en base64 (data URL o base64 puro).
 */
public class ConsentimientoGuardarRequest {

    @NotNull(message = "El ID del paciente es obligatorio")
    private Integer idPaciente;

    private Integer idHistorialClinico;

    @NotNull(message = "El tipo de procedimiento es obligatorio")
    private TipoProcedimiento tipoProcedimiento;

    @Size(max = 200)
    private String titulo;

    private String contenido;

    @Size(max = 20)
    private String version;

    @NotNull(message = "La aceptacion es obligatoria")
    private Boolean aceptado;

    private String firmaPacienteBase64;

    private String firmaOdontologoBase64;

    private String ipAddress;
    private String userAgent;

    // Getters y setters
    public Integer getIdPaciente() { return idPaciente; }
    public void setIdPaciente(Integer idPaciente) { this.idPaciente = idPaciente; }

    public Integer getIdHistorialClinico() { return idHistorialClinico; }
    public void setIdHistorialClinico(Integer idHistorialClinico) { this.idHistorialClinico = idHistorialClinico; }

    public TipoProcedimiento getTipoProcedimiento() { return tipoProcedimiento; }
    public void setTipoProcedimiento(TipoProcedimiento tipoProcedimiento) { this.tipoProcedimiento = tipoProcedimiento; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public Boolean getAceptado() { return aceptado; }
    public void setAceptado(Boolean aceptado) { this.aceptado = aceptado; }

    public String getFirmaPacienteBase64() { return firmaPacienteBase64; }
    public void setFirmaPacienteBase64(String firmaPacienteBase64) { this.firmaPacienteBase64 = firmaPacienteBase64; }

    public String getFirmaOdontologoBase64() { return firmaOdontologoBase64; }
    public void setFirmaOdontologoBase64(String firmaOdontologoBase64) { this.firmaOdontologoBase64 = firmaOdontologoBase64; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}
