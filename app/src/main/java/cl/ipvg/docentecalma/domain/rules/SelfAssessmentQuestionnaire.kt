package cl.ipvg.docentecalma.domain.rules

/**
 * Textos del cuestionario mínimo. Todas las preguntas usan la misma escala:
 * 1 = casi nunca … 5 = muy a menudo, en referencia a la **última semana**.
 * Puntuación más alta = mayor carga o tensión **percibida** (autoinforme).
 */
object SelfAssessmentQuestionnaire {

    val prompts: List<String> = listOf(
        "¿Con qué frecuencia sentiste que la carga de trabajo docente estuvo muy alta?",
        "¿Con qué frecuencia te costó desconectar o recuperarte entre una jornada y otra?",
        "¿Con qué frecuencia las situaciones en el aula te dejaron tensión o desgaste al cerrar el día?",
        "¿Con qué frecuencia sentiste que ibas “siempre apurado/a” o sin margen para pausas?"
    )

    /** Etiquetas cortas para lecturas agregadas (mismo orden que [prompts]). */
    val shortDimensionLabels: List<String> = listOf(
        "Carga docente percibida",
        "Recuperación entre jornadas",
        "Tensión vinculada al aula",
        "Ritmo y margen para pausas"
    )

    const val SCALE_LOW_LABEL: String = "Casi nunca"
    const val SCALE_HIGH_LABEL: String = "Muy a menudo"
}
