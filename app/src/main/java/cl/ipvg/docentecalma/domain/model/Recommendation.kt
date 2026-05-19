package cl.ipvg.docentecalma.domain.model

/**
 * Recomendación inmediata generada localmente a partir de un chequeo emocional.
 *
 * El contrato es plano y orientado a UI: cada campo corresponde 1-a-1 con un
 * elemento visible en `RecommendationsScreen`. El objeto no se persiste completo;
 * `RecommendationHistory` guarda solo un resumen (`shortMessage`).
 *
 * Campos obligatorios (contención breve + acción inmediata):
 * - [title] encabezado corto.
 * - [shortMessage] una frase empática, reconoce emoción e intensidad.
 * - [immediateAction] una sola acción concreta para hacer ahora.
 * - [breathingSuggestion] sugerencia de respiración breve.
 * - [whatToAvoid] una sola cosa que es mejor evitar en este estado.
 *
 * Campos opcionales:
 * - [optionalPedagogicalTip] consejo pedagógico aplicable a la próxima clase.
 * - [suggestedExercise] ejercicio del catálogo si corresponde.
 * - [severity] guía el CTA de derivación (chat, profesional).
 */
data class Recommendation(
    val emotion: Emotion,
    val intensity: Int,
    val title: String,
    val shortMessage: String,
    val immediateAction: String,
    val breathingSuggestion: String,
    val whatToAvoid: String,
    val optionalPedagogicalTip: String?,
    val severity: SeverityFlag,
    val suggestedExercise: QuickExercise? = null,
    /** Id de [cl.ipvg.docentecalma.domain.rules.MicromoduleCatalog] sugerido según la emoción. */
    val suggestedMicromoduleId: String? = null
) {
    /**
     * Deriva el tipo a registrar en el historial a partir del contenido.
     */
    fun toHistoryType(): RecommendationType = when {
        severity == SeverityFlag.SUGGEST_PROFESSIONAL -> RecommendationType.PROFESSIONAL
        severity == SeverityFlag.SUGGEST_CHAT -> RecommendationType.CHAT
        suggestedExercise != null -> RecommendationType.EXERCISE
        else -> RecommendationType.IMMEDIATE
    }
}
