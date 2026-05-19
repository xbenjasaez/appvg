package cl.ipvg.docentecalma.domain.model

/**
 * Catálogo estable de emociones soportadas por Docente Calma.
 *
 * - El [id] es la llave persistida en Room. No debe renombrarse sin una migración.
 * - [label] es el texto visible en español por defecto; el mapeo a UI vive en
 *   `domain/mapper/EmotionLabels.kt` para centralizar variantes (corta, detallada).
 * - [category] permite al motor de recomendaciones y al módulo de progreso
 *   agrupar emociones sin conocer cada caso.
 */
enum class Emotion(
    val id: String,
    val label: String,
    val category: EmotionCategory
) {
    STRESS("stress", "Estrés", EmotionCategory.DIFFICULT_HIGH_ACTIVATION),
    ANXIETY("anxiety", "Ansiedad", EmotionCategory.DIFFICULT_HIGH_ACTIVATION),
    ANGUST("angust", "Angustia", EmotionCategory.DIFFICULT_LOW_ENERGY),
    ANGER("anger", "Enojo", EmotionCategory.DIFFICULT_HIGH_ACTIVATION),
    SADNESS("sadness", "Tristeza", EmotionCategory.DIFFICULT_LOW_ENERGY),
    FRUSTRATION("frustration", "Frustración", EmotionCategory.DIFFICULT_HIGH_ACTIVATION),
    FATIGUE("fatigue", "Cansancio", EmotionCategory.DIFFICULT_LOW_ENERGY),
    CALM("calm", "Calma", EmotionCategory.REGULATED_POSITIVE),
    HAPPY("happy", "Feliz", EmotionCategory.REGULATED_POSITIVE);

    companion object {
        fun fromId(id: String): Emotion? = entries.firstOrNull { it.id == id }

        fun fromIdOrThrow(id: String): Emotion = fromId(id)
            ?: error("Emoción desconocida en persistencia: $id")
    }
}
