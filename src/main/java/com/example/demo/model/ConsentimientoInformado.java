package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Consentimiento informado firmado por el paciente.
 * Almacena el texto del documento, la aceptacion, la firma digital del paciente
 * (base64), opcionalmente la firma del odontologo y el PDF generado para respaldo legal.
 */
@Entity
@Table(name = "consentimiento_informado")
public class ConsentimientoInformado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_historial_clinico", nullable = false)
    private HistorialClinico historialClinico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoProcedimiento tipoProcedimiento;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    @Column(nullable = false, length = 20)
    private String version;

    @Column(nullable = false)
    private Boolean aceptado;

    private LocalDateTime fechaAceptacion;

    @Column(columnDefinition = "TEXT")
    private String firmaPacienteBase64;

    @Column(columnDefinition = "TEXT")
    private String firmaOdontologoBase64;

    private String ipAddress;
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String pdfRuta;

    @PrePersist
    protected void onCreate() {
        if (fechaAceptacion == null) {
            fechaAceptacion = LocalDateTime.now();
        }
        if (aceptado == null) {
            aceptado = false;
        }
        if (version == null || version.isBlank()) {
            version = "1.0";
        }
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public HistorialClinico getHistorialClinico() { return historialClinico; }
    public void setHistorialClinico(HistorialClinico historialClinico) { this.historialClinico = historialClinico; }

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

    public LocalDateTime getFechaAceptacion() { return fechaAceptacion; }
    public void setFechaAceptacion(LocalDateTime fechaAceptacion) { this.fechaAceptacion = fechaAceptacion; }

    public String getFirmaPacienteBase64() { return firmaPacienteBase64; }
    public void setFirmaPacienteBase64(String firmaPacienteBase64) { this.firmaPacienteBase64 = firmaPacienteBase64; }

    public String getFirmaOdontologoBase64() { return firmaOdontologoBase64; }
    public void setFirmaOdontologoBase64(String firmaOdontologoBase64) { this.firmaOdontologoBase64 = firmaOdontologoBase64; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getPdfRuta() { return pdfRuta; }
    public void setPdfRuta(String pdfRuta) { this.pdfRuta = pdfRuta; }
}
