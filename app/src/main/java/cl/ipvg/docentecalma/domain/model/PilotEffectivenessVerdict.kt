package cl.ipvg.docentecalma.domain.model

/**
 * Lectura conservadora de la efectividad percibida en el piloto (evidencia local).
 * No indica diagnóstico ni resultados clínicos.
 */
enum class PilotEffectivenessVerdict {
    /** Muy pocos registros: no es posible interpretar con criterio. */
    INSUFFICIENT_EVIDENCE,

    /** Hay una autoevaluación pero falta otra para comparar en el tiempo. */
    AWAITING_SECOND_SELF_ASSESSMENT,

    /** Uso registrado sin autoevaluaciones comparables. */
    USAGE_WITHOUT_SELF_ASSESSMENT_TREND,

    /** Uso sostenido y dos o más autoinformes, sin cambio claro favorable en la suma. */
    USAGE_WITHOUT_DEMONSTRATED_SHIFT,

    /** Mejora en autoinforme + satisfacción/ utilidad altas + uso en los últimos días. */
    INITIAL_POSITIVE_SIGNALS,

    /** Mejora en autoinforme pero satisfacción o utilidad no acompañan, o a la inversa. */
    MIXED_SIGNALS,

    /** Cambio favorable en autoinforme; falta encuesta breve o continuidad para cerrar la lectura. */
    POSITIVE_TREND_INCOMPLETE_PICTURE,

    /** Dos lecturas: cambio pequeño o estable; no alcanza umbral de “señal clara”. */
    SMALL_OR_UNCLEAR_CHANGE
}
