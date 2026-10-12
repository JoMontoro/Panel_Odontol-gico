package com.example.demo.model;

/**
 * Malestares / estados que el odontologo puede marcar sobre un diente del
 * odontograma. Un diente puede acumular varias condiciones a la vez
 * (ej. caries + endodoncia), por eso se guardan como una coleccion.
 */
public enum CondicionDental {
    CARIES,
    OBTURADO,
    AUSENTE,
    EXTRACCION_INDICADA,
    ENDODONCIA,
    CORONA,
    PROTESIS,
    IMPLANTE,
    FRACTURA,
    IMPACTADO,
    SUPERNUMERARIO,
    MOBILIDAD,
    SENSIBILIDAD,
    ERUPCIONADO
}
