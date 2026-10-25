package com.example.demo.service;

import com.example.demo.model.ConsentimientoInformado;
import com.example.demo.model.HistorialClinico;
import com.example.demo.model.Paciente;
import com.example.demo.model.TipoProcedimiento;
import com.example.demo.repository.ConsentimientoInformadoRepository;
import com.example.demo.repository.HistorialRepository;
import com.example.demo.repository.PacienteRepository;
import com.example.demo.dto.ConsentimientoGuardarRequest;
import com.example.demo.dto.ConsentimientoInformadoResponse;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Gestiona el Consentimiento Informado con firma del paciente.
 * Genera un PDF profesional con los datos del paciente, clausulas del consentimiento,
 * firma digital del paciente (base64) y firma del odontologo, listo para respaldo legal.
 */
@Service
public class ConsentimientoInformadoService {

    private static final String CARPETA_PDF = "uploads" + File.separator + "consentimientos";
    private static final String NOMBRE_CLINICA = "CONSULTORIO ODONTOLOGICO";
    private static final String DIRECCION_CLINICA = "Av. Principal 123 - Ciudad";
    private static final String TELEFONO_CLINICA = "+54 9 11 2345-6789";
    private static final String EMAIL_CLINICA = "consultorio@odontologico.com";

    @Autowired
    private ConsentimientoInformadoRepository repo;

    @Autowired
    private HistorialRepository historialRepo;

    @Autowired
    private PacienteRepository pacienteRepo;

    @Autowired
    private SesionActual sesion;

    /** Guarda/acepta un consentimiento informado y genera su PDF con firma. */
    public ConsentimientoInformado guardar(ConsentimientoGuardarRequest req, HttpServletRequest httpRequest) {
        com.example.demo.model.Usuario actual = sesion.exigirEscritura(); // solo odontologo puede escribir

        Paciente paciente = pacienteRepo.findById(req.getIdPaciente())
                .orElseThrow(() -> new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Paciente no encontrado"));

        // Aislamiento de cartera: solo el dueno puede registrar consentimiento de su paciente
        if (!sesion.esDeMiCartera(paciente)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,
                    "Ese paciente pertenece a la cartera de otro odontologo.");
        }

        // Validar que si acepta, traiga la firma del paciente (requisito legal)
        if (Boolean.TRUE.equals(req.getAceptado()) && (req.getFirmaPacienteBase64() == null
                || req.getFirmaPacienteBase64().isBlank())) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Para aceptar el consentimiento es obligatoria la firma del paciente.");
        }

        // Historial clinico (asociar si se envia, sino buscar/crear)
        HistorialClinico historial = null;
        if (req.getIdHistorialClinico() != null) {
            historial = historialRepo.findById(req.getIdHistorialClinico())
                    .orElseThrow(() -> new ResponseStatusException(
                            org.springframework.http.HttpStatus.NOT_FOUND, "Historial clinico no encontrado"));
        } else {
            historial = historialRepo.findByPaciente(paciente).orElse(null);
            if (historial == null) {
                historial = new HistorialClinico();
                historial.setPaciente(paciente);
                historial = historialRepo.save(historial);
            }
        }

        // Evitar duplicar un consentimiento ya aceptado para el mismo procedimiento en el mismo historial
        if (Boolean.TRUE.equals(req.getAceptado())) {
            Optional<ConsentimientoInformado> previo = repo.findByHistorialClinico(historial);
            if (previo.isPresent() && Boolean.TRUE.equals(previo.get().getAceptado())
                    && previo.get().getTipoProcedimiento() == req.getTipoProcedimiento()) {
                throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,
                        "Ya existe un consentimiento aceptado para este procedimiento en este historial.");
            }
        }

        ConsentimientoInformado c = new ConsentimientoInformado();
        c.setPaciente(paciente);
        c.setHistorialClinico(historial);
        c.setTipoProcedimiento(req.getTipoProcedimiento());
        c.setTitulo(req.getTitulo() != null ? req.getTitulo()
                : "Consentimiento informado - " + req.getTipoProcedimiento().getEtiqueta());
        c.setContenido(req.getContenido());
        c.setVersion(req.getVersion() != null ? req.getVersion() : "1.0");
        c.setAceptado(req.getAceptado());
        c.setFirmaPacienteBase64(req.getFirmaPacienteBase64());
        c.setFirmaOdontologoBase64(req.getFirmaOdontologoBase64());
        c.setIpAddress(httpRequest.getRemoteAddr());
        c.setUserAgent(httpRequest.getHeader("User-Agent"));

        c = repo.save(c);

        // Generar PDF con firma (solo si se acepto, para respaldo legal)
        if (Boolean.TRUE.equals(c.getAceptado())) {
            generarYGuardarPdf(c, actual);
        }

        return c;
    }

    /** Obtiene el consentimiento aceptado de un historial (version vigente). */
    public Optional<ConsentimientoInformado> obtenerPorHistorial(HistorialClinico historial) {
        return repo.findByHistorialClinico(historial);
    }

    /** Listado resumido de consentimientos de la cartera. */
    public List<ConsentimientoInformadoResponse> listarResumido() {
        Integer odoId = sesion.odontologoId();
        List<ConsentimientoInformado> lista = (odoId == SesionActual.SIN_FILTRO)
                ? repo.findAll()
                : repo.listarDeCartera(odoId);
        return lista.stream().map(c -> new ConsentimientoInformadoResponse(
                c.getId(),
                c.getPaciente() != null ? c.getPaciente().getId() : null,
                nombreCompleto(c.getPaciente()),
                c.getTipoProcedimiento(),
                c.getTitulo(),
                c.getVersion(),
                c.getAceptado(),
                c.getFechaAceptacion(),
                c.getFirmaPacienteBase64() != null && !c.getFirmaPacienteBase64().isBlank(),
                c.getFirmaOdontologoBase64() != null && !c.getFirmaOdontologoBase64().isBlank(),
                c.getIpAddress(),
                c.getPdfRuta()
        )).collect(Collectors.toList());
    }

    /** Detalle completo de un consentimiento (incluye base64 de firmas y contenido). */
    public ConsentimientoInformado obtenerDetalle(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Consentimiento no encontrado"));
    }

    /** Descarga el PDF generado del consentimiento. */
    public ResponseEntity<Resource> descargarPdf(Integer id) {
        ConsentimientoInformado c = obtenerDetalle(id);

        // Aislamiento de lectura
        if (!sesion.esDeMiCartera(c.getPaciente()) && !sesion.esAdmin(sesion.usuario())) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,
                    "No autorizado para descargar este documento.");
        }

        if (c.getPdfRuta() == null || c.getPdfRuta().isBlank()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,
                    "El PDF del consentimiento no se encuentra disponible.");
        }

        try {
            Path path = Paths.get(c.getPdfRuta());
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"consentimiento_" + c.getId() + ".pdf\"")
                        .body(resource);
            } else {
                throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,
                        "Archivo PDF no encontrado en el servidor.");
            }
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al leer el archivo PDF.");
        }
    }

    /* ===================== Generacion de PDF profesional ===================== */

    private void generarYGuardarPdf(ConsentimientoInformado c, com.example.demo.model.Usuario actual) {
        try {
            // Crear carpeta si no existe
            File carpeta = new File(CARPETA_PDF);
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }

            String nombreArchivo = "consentimiento_" + c.getId() + "_" + UUID.randomUUID().toString().substring(0, 8) + ".pdf";
            Path archivo = Paths.get(CARPETA_PDF, nombreArchivo);

            Document document = new Document(PageSize.A4, 50, 50, 70, 70);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(archivo.toFile()));
            writer.setPageEvent(new PieDePagina());
            document.open();

            // --- Encabezado de la clinica ---
            Font fuenteTituloClinica = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font fuenteSubClinica = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);

            Paragraph clinica = new Paragraph(NOMBRE_CLINICA, fuenteTituloClinica);
            clinica.setAlignment(Element.ALIGN_CENTER);
            document.add(clinica);

            Paragraph datosClinica = new Paragraph(DIRECCION_CLINICA + "  |  " + TELEFONO_CLINICA + "  |  " + EMAIL_CLINICA, fuenteSubClinica);
            datosClinica.setAlignment(Element.ALIGN_CENTER);
            document.add(datosClinica);

            Paragraph linea = new Paragraph("--------------------------------------------------------------------------------");
            linea.setAlignment(Element.ALIGN_CENTER);
            documentoEspacio(document, 10);
            document.add(linea);

            // --- Titulo del documento ---
            Font fuenteTituloDoc = new Font(Font.FontFamily.HELVETICA, 15, Font.BOLD);
            Paragraph tituloDoc = new Paragraph("CONSENTIMIENTO INFORMADO", fuenteTituloDoc);
            tituloDoc.setAlignment(Element.ALIGN_CENTER);
            document.add(tituloDoc);

            Font fuenteSub = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
            Paragraph subtipo = new Paragraph("Procedimiento: " + c.getTipoProcedimiento().getEtiqueta()
                    + "    |    Versión: " + c.getVersion(), fuenteSub);
            subtipo.setAlignment(Element.ALIGN_CENTER);
            document.add(subtipo);
            documentoEspacio(document, 8);

            // --- Datos del paciente en tabla ---
            PdfPTable tablaDatos = new PdfPTable(2);
            tablaDatos.setWidthPercentage(100);
            tablaDatos.setWidths(new float[]{30, 70});

            Font fuenteEtiqueta = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
            Font fuenteValor = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);

            addCelda(tablaDatos, "Paciente:", fuenteEtiqueta, false);
            addCelda(tablaDatos, nombreCompleto(c.getPaciente()), fuenteValor, false);
            addCelda(tablaDatos, "DNI / Documento:", fuenteEtiqueta, false);
            addCelda(tablaDatos, c.getPaciente().getDni() != null ? c.getPaciente().getDni() : "—", fuenteValor, false);
            addCelda(tablaDatos, "Fecha de nacimiento:", fuenteEtiqueta, false);
            addCelda(tablaDatos, c.getPaciente().getFechaNacimiento() != null
                    ? c.getPaciente().getFechaNacimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : "—", fuenteValor, false);
            addCelda(tablaDatos, "Teléfono:", fuenteEtiqueta, false);
            addCelda(tablaDatos, c.getPaciente().getTelefono() != null ? c.getPaciente().getTelefono() : "—", fuenteValor, false);
            addCelda(tablaDatos, "Email:", fuenteEtiqueta, false);
            addCelda(tablaDatos, c.getPaciente().getEmail() != null ? c.getPaciente().getEmail() : "—", fuenteValor, false);

            document.add(tablaDatos);
            documentoEspacio(document, 10);

            // --- Contenido / clausulas del consentimiento ---
            Font fuenteTexto = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
            Paragraph etiquetaClausulas = new Paragraph("Cláusulas del consentimiento:", fuenteEtiqueta);
            document.add(etiquetaClausulas);
            documentoEspacio(document, 4);

            String contenido = c.getContenido() != null ? c.getContenido() : textoPorDefecto(c.getTipoProcedimiento());
            Paragraph parrafoContenido = new Paragraph(contenido, fuenteTexto);
            parrafoContenido.setAlignment(Element.ALIGN_JUSTIFIED);
            document.add(parrafoContenido);
            documentoEspacio(document, 10);

            // --- Declaracion de aceptacion ---
            Paragraph declaracion = new Paragraph(
                    "El/la paciente declara haber leído, comprendido y aceptado voluntariamente los términos y condiciones "
                    + "expuestos en el presente consentimiento informado, autorizando al odontólogo a realizar el procedimiento "
                    + "indicado, así como cualquier otra actuación complementaria que se considere necesaria para el adecuado "
                    + "desarrollo del tratamiento.",
                    fuenteTexto);
            declaracion.setAlignment(Element.ALIGN_JUSTIFIED);
            document.add(declaracion);
            documentoEspacio(document, 16);

            // --- Firmas ---
            PdfPTable tablaFirmas = new PdfPTable(2);
            tablaFirmas.setWidthPercentage(100);
            tablaFirmas.setWidths(new float[]{50, 50});

            // Columna izquierda: Firma del paciente
            PdfPCell celdaPaciente = new PdfPCell();
            celdaPaciente.setBorder(Rectangle.NO_BORDER);
            celdaPaciente.setPadding(0);

            Paragraph lblPaciente = new Paragraph("Firma del paciente", new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD));
            lblPaciente.setAlignment(Element.ALIGN_CENTER);
            celdaPaciente.addElement(lblPaciente);
            documentoEspacioCelda(celdaPaciente, 4);

            if (c.getFirmaPacienteBase64() != null && !c.getFirmaPacienteBase64().isBlank()) {
                try {
                    Image imgPaciente = decodificarImagenFirma(c.getFirmaPacienteBase64());
                    imgPaciente.scaleToFit(160, 60);
                    imgPaciente.setAlignment(Element.ALIGN_CENTER);
                    celdaPaciente.addElement(imgPaciente);
                } catch (Exception e) {
                    Paragraph err = new Paragraph("[Firma no disponible]",
                            new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC));
                    err.setAlignment(Element.ALIGN_CENTER);
                    celdaPaciente.addElement(err);
                }
            } else {
                Paragraph lineaFirma = new Paragraph(
                        "________________________________",
                        new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL));
                lineaFirma.setAlignment(Element.ALIGN_CENTER);
                celdaPaciente.addElement(lineaFirma);
            }

            Paragraph fechaPaciente = new Paragraph("Fecha: " + fechaFormateada(c.getFechaAceptacion()),
                    new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL));
            fechaPaciente.setAlignment(Element.ALIGN_CENTER);
            celdaPaciente.addElement(fechaPaciente);

            tablaFirmas.addCell(celdaPaciente);

            // Columna derecha: Firma del odontologo
            PdfPCell celdaOdonto = new PdfPCell();
            celdaOdonto.setBorder(Rectangle.NO_BORDER);
            celdaOdonto.setPadding(0);

            Paragraph lblOdonto = new Paragraph("Firma del odontólogo responsable",
                    new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD));
            lblOdonto.setAlignment(Element.ALIGN_CENTER);
            celdaOdonto.addElement(lblOdonto);
            documentoEspacioCelda(celdaOdonto, 4);

            if (c.getFirmaOdontologoBase64() != null && !c.getFirmaOdontologoBase64().isBlank()) {
                try {
                    Image imgOdonto = decodificarImagenFirma(c.getFirmaOdontologoBase64());
                    imgOdonto.scaleToFit(160, 60);
                    imgOdonto.setAlignment(Element.ALIGN_CENTER);
                    celdaOdonto.addElement(imgOdonto);
                } catch (Exception e) {
                    Paragraph err = new Paragraph("[Firma no disponible]",
                            new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC));
                    err.setAlignment(Element.ALIGN_CENTER);
                    celdaOdonto.addElement(err);
                }
            } else {
                Paragraph lineaOdonto = new Paragraph(
                        "________________________________",
                        new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL));
                lineaOdonto.setAlignment(Element.ALIGN_CENTER);
                celdaOdonto.addElement(lineaOdonto);
            }

            Paragraph nombreOdonto = new Paragraph(actual.getNombre() + " " + actual.getApellido(),
                    new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL));
            nombreOdonto.setAlignment(Element.ALIGN_CENTER);
            celdaOdonto.addElement(nombreOdonto);

            Paragraph matriculaOdonto = new Paragraph("Mat. Odontólogo",
                    new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC));
            matriculaOdonto.setAlignment(Element.ALIGN_CENTER);
            celdaOdonto.addElement(matriculaOdonto);

            tablaFirmas.addCell(celdaOdonto);

            document.add(tablaFirmas);

            documentoEspacio(document, 20);

            // --- Folio / seguridad ---
            Font fuenteFolio = new Font(Font.FontFamily.HELVETICA, 7, Font.ITALIC);
            Paragraph folio = new Paragraph(
                    "Folio N°: " + c.getId() + "   |   Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    + "   |   IP: " + c.getIpAddress() + "   |   Documento protegido: cualquier alteración invalida su valor legal.",
                    fuenteFolio);
            folio.setAlignment(Element.ALIGN_CENTER);
            document.add(folio);

            document.close();

            // Guardar ruta relativa en BD
            c.setPdfRuta(archivo.toString());
            repo.save(c);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al generar el PDF del consentimiento: " + e.getMessage());
        }
    }

    private Image decodificarImagenFirma(String base64) throws IOException, BadElementException {
        String limpio = base64.trim();
        // Si viene como data URL, quitar el prefijo
        if (limpio.contains("base64,")) {
            limpio = limpio.substring(limpio.indexOf("base64,") + 7);
        }
        byte[] bytes = Base64.getDecoder().decode(limpio);
        return Image.getInstance(bytes);
    }

    private void documentoEspacio(com.itextpdf.text.Document doc, int puntos) throws Exception {
        doc.add(new Paragraph(" "));
        for (int i = 1; i < puntos / 10; i++) {
            doc.add(new Paragraph(" "));
        }
    }

    private void documentoEspacioCelda(PdfPCell celda, int puntos) throws Exception {
        for (int i = 0; i < puntos; i++) {
            celda.addElement(new Paragraph(" "));
        }
    }

    private void addCelda(PdfPTable tabla, String texto, Font fuente, boolean encabezado) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        if (encabezado) {
            celda.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
        }
        celda.setBorderWidth(0.5f);
        tabla.addCell(celda);
    }

    private String nombreCompleto(Paciente p) {
        if (p == null) return "—";
        return ((p.getNombre() != null ? p.getNombre() : "") + " "
                + (p.getApellido() != null ? p.getApellido() : "")).trim();
    }

    private String fechaFormateada(LocalDateTime fecha) {
        if (fecha == null) return "—";
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private String textoPorDefecto(TipoProcedimiento tipo) {
        if (tipo == null) {
            return "Consiento voluntariamente el procedimiento odontológico indicado, después de haber sido informado/a "
                    + "sobre el tratamiento, sus riesgos, beneficios y alternativas.";
        }
        switch (tipo) {
            case EXTRACION_DENTAL:
                return "Consiento voluntariamente la extracción dental indicada por el odontólogo, "
                        + "entendiendo los riesgos, beneficios y alternativas explicados, incluyendo posible sangrado, infección, "
                        + "dolor postoperatorio y necesidad de cuidados posteriores.";
            case IMPLANTES:
                return "Consiento la colocación de implantes dentales, comprendiendo que el proceso puede requerir "
                        + "varias etapas, cirugía y tiempo de osteointegración, aceptando los riesgos asociados.";
            case ENDODONCIA:
                return "Consiento el tratamiento de conducto (endodoncia) indicado, entendiendo que se busca conservar "
                        + "la pieza dental y que existen riesgos como fractura o reinfección.";
            case ORTONCIA:
                return "Consiento el tratamiento de ortodoncia propuesto, comprendiendo su duración, cuidados y posible "
                        + "necesidad de contención postratamiento.";
            case BLANQUEAMIENTO:
                return "Consiento el procedimiento de blanqueamiento dental, entiendo los posibles efectos de sensibilidad dental.";
            case CIRUGIA_ORAL:
                return "Consiento la cirugía oral indicada, aceptando los riesgos quirúrgicos y el cuidado postoperatorio.";
            case PERIODONCIA:
                return "Consiento el tratamiento periodontal indicado para el control de enfermedad de las encías.";
            case PROTESIS:
                return "Consiento la rehabilitación con prótesis dental, entendiendo las etapas de toma de medidas, pruebas y colocación.";
            default:
                return "Consiento voluntariamente el procedimiento odontológico indicado, después de haber sido informado/a "
                        + "sobre el tratamiento, sus riesgos, beneficios y alternativas.";
        }
    }

    /* ===================== Pie de pagina de cada hoja ===================== */

    private class PieDePagina extends com.itextpdf.text.pdf.PdfPageEventHelper {
        @Override
        public void onEndPage(com.itextpdf.text.pdf.PdfWriter writer, com.itextpdf.text.Document document) {
            com.itextpdf.text.pdf.PdfContentByte cb = writer.getDirectContent();
            cb.saveState();
            Font font = new Font(Font.FontFamily.HELVETICA, 7, Font.ITALIC);
            cb.beginText();
            cb.setFontAndSize(font.getBaseFont(), font.getSize());
            cb.showTextAligned(com.itextpdf.text.Element.ALIGN_CENTER,
                    NOMBRE_CLINICA + " - Consentimiento Informado Folio " + document.getPageNumber(),
                    (PageSize.A4.getWidth() / 2), 30, 0);
            cb.endText();
            cb.restoreState();
        }
    }
}
