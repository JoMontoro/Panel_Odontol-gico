package com.example.demo.model;

/**
 * Valoración de cada zona de tejido blando de la cavidad oral.
 * NO_EVALUADO deja constancia de que la zona no se reviso en esa consulta.
 */
public enum EstadoTejidoBlando {
    NORMAL,
    INFLAMACION,
    SANGRADO,
    ULCERA,
    LESION,
    TRAUMA,
    ATROFIA,
    NEOPLASIA,
    NO_EVALUADO
}
