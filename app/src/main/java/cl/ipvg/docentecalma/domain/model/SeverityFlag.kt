package cl.ipvg.docentecalma.domain.model

/**
 * Nivel de alerta de una recomendación. Guía el CTA de la pantalla
 * (chat de apoyo, contacto profesional, etc.).
 */
enum class SeverityFlag {
    NORMAL,
    SUGGEST_CHAT,
    SUGGEST_PROFESSIONAL
}
