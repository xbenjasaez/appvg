package cl.ipvg.docentecalma.domain.model

/**
 * Destino sugerido en la app a partir del resultado de la autoevaluación.
 */
enum class SelfAssessmentNavHint {
    EMOTIONAL_CHECK_IN,
    QUICK_EXERCISES,
    CLASSROOM_GUIDANCE,
    SUPPORT_CHAT
}

data class SelfAssessmentSuggestion(
    val title: String,
    val description: String,
    val navHint: SelfAssessmentNavHint
)
