package com.example.demo.model;

public enum TipoProcedimiento {
    EXTRACION_DENTAL("Extracción dental"),
    IMPLANTES("Implantes dentales"),
    ENDODONCIA("Endodoncia / Tratamiento de conducto"),
    ORTONCIA("Ortodoncia"),
    BLANQUEAMIENTO("Blanqueamiento dental"),
    CIRUGIA_ORAL("Cirugía oral"),
    PERIODONCIA("Periodoncia"),
    PROTESIS("Prótesis dental"),
    GENERAL("Consentimiento general");

    private final String etiqueta;

    TipoProcedimiento(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
