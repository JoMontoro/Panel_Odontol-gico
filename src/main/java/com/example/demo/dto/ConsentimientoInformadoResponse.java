package com.example.demo.dto;

import com.example.demo.model.TipoProcedimiento;
import java.time.LocalDateTime;

public class ConsentimientoInformadoResponse {

    private Integer id;
    private Integer idPaciente;
    private String nombrePaciente;
    private TipoProcedimiento tipoProcedimiento;
    private String titulo;
    private String version;
    private Boolean aceptado;
    private LocalDateTime fechaAceptacion;
    private boolean tieneFirmaPaciente;
    private boolean tieneFirmaOdontologo;
    private String ipAddress;
    private String pdfRuta;

    public ConsentimientoInformadoResponse() {}

    public ConsentimientoInformadoResponse(Integer id, Integer idPaciente, String nombrePaciente,
            TipoProcedimiento tipoProcedimiento, String titulo, String version, Boolean aceptado,
            LocalDateTime fechaAceptacion, boolean tieneFirmaPaciente, boolean tieneFirmaOdontologo,
            String ipAddress, String pdfRuta) {
        this.id = id;
        this.idPaciente = idPaciente;
        this.nombrePaciente = nombrePaciente;
        this.tipoProcedimiento = tipoProcedimiento;
        this.titulo = titulo;
        this.version = version;
        this.aceptado = aceptado;
        this.fechaAceptacion = fechaAceptacion;
        this.tieneFirmaPaciente = tieneFirmaPaciente;
        this.tieneFirmaOdontologo = tieneFirmaOdontologo;
        this.ipAddress = ipAddress;
        this.pdfRuta = pdfRuta;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getIdPaciente() { return idPaciente; }
    public void setIdPaciente(Integer idPaciente) { this.idPaciente = idPaciente; }
    public String getNombrePaciente() { return nombrePaciente; }
    public void setNombrePaciente(String nombrePaciente) { this.nombrePaciente = nombrePaciente; }
    public TipoProcedimiento getTipoProcedimiento() { return tipoProcedimiento; }
    public void setTipoProcedimiento(TipoProcedimiento tipoProcedimiento) { this.tipoProcedimiento = tipoProcedimiento; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public Boolean getAceptado() { return aceptado; }
    public void setAceptado(Boolean aceptado) { this.aceptado = aceptado; }
    public LocalDateTime getFechaAceptacion() { return fechaAceptacion; }
    public void setFechaAceptacion(LocalDateTime fechaAceptacion) { this.fechaAceptacion = fechaAceptacion; }
    public boolean isTieneFirmaPaciente() { return tieneFirmaPaciente; }
    public void setTieneFirmaPaciente(boolean tieneFirmaPaciente) { this.tieneFirmaPaciente = tieneFirmaPaciente; }
    public boolean isTieneFirmaOdontologo() { return tieneFirmaOdontologo; }
    public void setTieneFirmaOdontologo(boolean tieneFirmaOdontologo) { this.tieneFirmaOdontologo = tieneFirmaOdontologo; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getPdfRuta() { return pdfRuta; }
    public void setPdfRuta(String pdfRuta) { this.pdfRuta = pdfRuta; }
}
