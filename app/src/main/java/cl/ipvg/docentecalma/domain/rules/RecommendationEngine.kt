package cl.ipvg.docentecalma.domain.rules

import cl.ipvg.docentecalma.domain.mapper.EmotionLabels
import cl.ipvg.docentecalma.domain.mapper.displayName
import cl.ipvg.docentecalma.domain.model.Emotion
import cl.ipvg.docentecalma.domain.model.EmotionCategory
import cl.ipvg.docentecalma.domain.model.EmotionalCheckIn
import cl.ipvg.docentecalma.domain.model.Recommendation
import cl.ipvg.docentecalma.domain.model.SeverityFlag
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Motor local de recomendaciones.
 *
 * Responsabilidades:
 * - Tomar un `EmotionalCheckIn` y componer una `Recommendation` plana.
 * - Usar el `EmotionRuleCatalog` para el contenido específico por emoción.
 * - Derivar `shortMessage` y `severity` a partir de la categoría e intensidad.
 *
 * No hace red, no usa IA, no depende de Android. Es un simple ensamblador de
 * plantillas, por lo tanto totalmente testeable con unit tests.
 */
@Singleton
class RecommendationEngine @Inject constructor() {

    fun build(checkIn: EmotionalCheckIn): Recommendation {
        val emotion = checkIn.emotion
        val intensity = checkIn.intensity
        val rule = EmotionRuleCatalog.ruleFor(emotion)
        val exercise = rule.suggestedExerciseId?.let(QuickExerciseCatalog::byId)

        return Recommendation(
            emotion = emotion,
            intensity = intensity,
            title = rule.title,
            shortMessage = shortMessageFor(emotion, intensity),
            immediateAction = rule.immediateAction,
            breathingSuggestion = rule.breathingSuggestion,
            whatToAvoid = rule.whatToAvoid,
            optionalPedagogicalTip = rule.optionalPedagogicalTip,
            severity = severityFor(emotion.category, intensity),
            suggestedExercise = exercise,
            suggestedMicromoduleId = MicromoduleCatalog.suggestedIdFor(emotion)
        )
    }

    /**
     * Mensaje corto y empático. Reconoce la emoción y su nivel sin diagnosticar.
     * Las emociones reguladas (CALM, HAPPY) reciben refuerzo positivo;
     * las difíciles reciben contención proporcional al bucket.
     */
    private fun shortMessageFor(emotion: Emotion, intensity: Int): String {
        val label = emotion.displayName.lowercase()
        val intensityLabel = EmotionLabels.intensityLabel(intensity).lowercase()
        return when (emotion.category) {
            EmotionCategory.REGULATED_POSITIVE -> when (bucketOf(intensity)) {
                Bucket.LOW, Bucket.MID ->
                    "Qué bueno notar $label ($intensityLabel). Registrarla también cuida tu bienestar."
                Bucket.HIGH ->
                    "Estás en $label ($intensityLabel). Es un gran recurso para tu día."
            }
            EmotionCategory.DIFFICULT_HIGH_ACTIVATION -> when (bucketOf(intensity)) {
                Bucket.LOW ->
                    "Aparece algo de $label ($intensityLabel). Buena señal que lo notes a tiempo."
                Bucket.MID ->
                    "Notas $label a nivel $intensityLabel. Atiéndela antes de seguir."
                Bucket.HIGH ->
                    "La $label está $intensityLabel. Cuídate primero; el trabajo puede esperar unos minutos."
            }
            EmotionCategory.DIFFICULT_LOW_ENERGY -> when (bucketOf(intensity)) {
                Bucket.LOW ->
                    "Aparece $label en nivel $intensityLabel. Permítete un pequeño respiro."
                Bucket.MID ->
                    "Estás con $label $intensityLabel. Hacer algo suave puede ayudarte a recuperarte."
                Bucket.HIGH ->
                    "La $label es $intensityLabel. Es válido detenerte y pedir apoyo."
            }
        }
    }

    /**
     * Severidad derivada. Solo las emociones difíciles escalan; las reguladas
     * siempre son NORMAL. En intensidad 4-5 se sugiere derivación profesional.
     */
    private fun severityFor(category: EmotionCategory, intensity: Int): SeverityFlag = when {
        category == EmotionCategory.REGULATED_POSITIVE -> SeverityFlag.NORMAL
        intensity <= 2 -> SeverityFlag.NORMAL
        intensity == 3 -> SeverityFlag.SUGGEST_CHAT
        else -> SeverityFlag.SUGGEST_PROFESSIONAL
    }

    private fun bucketOf(intensity: Int): Bucket = when (intensity) {
        in 1..2 -> Bucket.LOW
        3 -> Bucket.MID
        else -> Bucket.HIGH
    }

    private enum class Bucket { LOW, MID, HIGH }
}
